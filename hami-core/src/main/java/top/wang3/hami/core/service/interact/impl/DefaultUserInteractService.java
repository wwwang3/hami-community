package top.wang3.hami.core.service.interact.impl;

import com.alibaba.otter.canal.common.utils.ExecutorTemplate;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.dto.interact.LikeType;
import top.wang3.hami.common.message.interact.CollectRabbitMessage;
import top.wang3.hami.common.message.interact.FollowRabbitMessage;
import top.wang3.hami.common.message.interact.LikeRabbitMessage;
import top.wang3.hami.common.model.*;
import top.wang3.hami.common.util.InteractHandler;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.common.util.ZPageHandler;
import top.wang3.hami.core.cache.CacheService;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.exception.HamiServiceException;
import top.wang3.hami.core.service.article.cache.ArticleCacheService;
import top.wang3.hami.core.service.comment.repository.CommentRepository;
import top.wang3.hami.core.service.interact.UserInteractService;
import top.wang3.hami.core.service.interact.repository.CollectRepository;
import top.wang3.hami.core.service.interact.repository.FollowRepository;
import top.wang3.hami.core.service.interact.repository.LikeRepository;
import top.wang3.hami.core.service.user.cache.UserCacheService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultUserInteractService implements UserInteractService {

    private final CacheService cacheService;
    private final RabbitMessagePublisher rabbitMessagePublisher;
    private final ThreadPoolTaskExecutor taskExecutor;

    private final ArticleCacheService articleCacheService;
    private final UserCacheService userCacheService;

    private final LikeRepository likeRepository;
    private final CollectRepository collectRepository;
    private final FollowRepository followRepository;
    private final CommentRepository commentRepository;



    @Override
    public boolean likeAction(int userId, int itemId, LikeType likeType, boolean state) {
        // 先写Redis 判断用户是否点赞和查询用户点赞列表都是从Redis读取
        // 当MySQL某个记录写入失败时且没有重试措施，会出现二者不一致问题
        String key = RedisConstants.USER_LIKE_LIST + likeType.getType() + ":" + userId;
        return InteractHandler
                .<Integer>build("点赞")
                .of(key, itemId, state)
                .millis(TimeoutConstants.LIKE_LIST_EXPIRE)
                .loader(() -> loadLikeList(userId, likeType))
                .preCheck(member -> {
                    if (getItemUser(member, likeType) == null) {
                        throw new HamiServiceException("item不存在");
                    }
                })
                .postAct(() -> {
                    // 执行成功的后置处理器. 发送MQ消息异步写入DB
                    LikeRabbitMessage message = new LikeRabbitMessage(
                            userId,
                            getItemUser(itemId, likeType),
                            state ? Constants.ONE : Constants.ZERO,
                            itemId,
                            likeType
                    );
                    rabbitMessagePublisher.publishMsg(message);
                })
                .execute();
    }

    @Override
    public boolean collectAction(int userId, int itemId, boolean state) {
        String key = RedisConstants.USER_COLLECT_LIST + userId;
        return InteractHandler
                .<Integer>build("收藏")
                .of(key, itemId, state)
                .millis(TimeoutConstants.COLLECT_LIST_EXPIRE)
                .loader(() -> loadCollectList(userId))
                .preCheck(member -> {
                    if (getItemUser(member, LikeType.ARTICLE) == null) {
                        throw new HamiServiceException("文章不存在");
                    }
                })
                .postAct(() -> {
                    CollectRabbitMessage message = new CollectRabbitMessage(
                            userId,
                            getItemUser(itemId, LikeType.ARTICLE),
                            state ? Constants.ONE : Constants.ZERO,
                            itemId
                    );
                    rabbitMessagePublisher.publishMsgSync(message);
                })
                .execute();
    }

    @Override
    public boolean followAction(int userId, int followingId, boolean state) {
        String key = RedisConstants.USER_FOLLOWING_LIST + userId;
        return InteractHandler
                .<Integer>build("点赞")
                .of(key, followingId, state)
                .millis(TimeoutConstants.FOLLOWING_LIST_EXPIRE)
                .loader(() -> loadFollowList(userId))
                .preCheck(member -> {
                    if (Objects.equals(userId, member)) {
                        throw new HamiServiceException("你时时刻刻都在关注你自己~");
                    }
                })
                .preCheck(this::checkUserId)
                .postAct(() -> {
                    // 执行成功的后置处理器. 发送MQ消息异步写入DB
                    FollowRabbitMessage message = new FollowRabbitMessage(
                            userId,
                            followingId,
                            state ? Constants.ONE : Constants.ZERO,
                            null
                    );
                    rabbitMessagePublisher.publishMsgSync(message);
                })
                .execute();
    }

    @Override
    public int getLikeCount(int userId, LikeType likeType) {
        return getInteractCount(userId, RedisConstants.LIKE_INTERACT_HKEY + likeType.getType());
    }

    @Override
    public int getCollectCount(int userId) {
        return getInteractCount(userId, RedisConstants.COLLECT_INTERACT_HKEY);
    }

    @Override
    public int getFollowCount(int userId) {
        return getInteractCount(userId, RedisConstants.FOLLOW_INTERACT_HKEY);
    }

    @Override
    public int getFollowerCount(int userId) {
        return getInteractCount(userId, RedisConstants.FOLLOWER_INTERACT_HKEY);
    }

    @Override
    public List<Integer> getLikeList(Page<LikeItem> page, int userId, LikeType likeType) {
        // 点赞列表
        String key = RedisConstants.USER_LIKE_LIST + likeType.getType() + ":" + userId;
        return ZPageHandler
                .<Integer>of(key, page)
                .countSupplier(() -> getLikeCount(userId, likeType))
                .loader(() -> loadLikeList(userId, LikeType.ARTICLE))
                .query();
    }

    @Override
    public List<Integer> getCollectList(Page<ArticleCollect> page, int userId) {
        // 收藏列表
        String key = RedisConstants.USER_COLLECT_LIST + userId;
        return ZPageHandler
                .<Integer>of(key, page)
                .countSupplier(() -> getCollectCount(userId))
                .loader(() -> loadCollectList(userId))
                .query();
    }

    @Override
    public List<Integer> getFollowList(Page<UserFollow> page, int userId) {
        // 关注列表
        String key = RedisConstants.USER_FOLLOWING_LIST + userId;
        return ZPageHandler
                .<Integer>of(key, page)
                .countSupplier(() -> getFollowCount(userId))
                .loader(() -> loadFollowList(userId))
                .query();
    }

    @Override
    public List<Integer> getFollowerList(Page<UserFollow> page, int userId) {
        // 粉丝列表
        String key = RedisConstants.USER_FOLLOWER_LIST + userId;
        return ZPageHandler
                .<Integer>of(key, page)
                .countSupplier(() -> getFollowerCount(userId))
                .source((current, size) -> {
                    Page<UserFollow> followerPage = new Page<>(current, size, false);
                    return followRepository.listUserFollowers(followerPage, userId);
                })
                .loader(() -> loadFollowerList(userId))
                .query();
    }

    @Override
    public List<Integer> loadLikeList(int userId, LikeType likeType) {
        String key = RedisConstants.USER_LIKE_LIST + likeType.getType() + ":" + userId;
        // 全部加载出来, 后续重构(只加载部分)的话, 需要重构点赞状态判断逻辑, 目前是基于zset判断的
        List<LikeItem> likeItems = likeRepository.listUserLikeItem(userId, likeType);
        if (CollectionUtils.isEmpty(likeItems)) {
            return Collections.emptyList();
        }
        List<Integer> results = new ArrayList<>(likeItems.size());
        Collection<Tuple> tuples = ListMapperHandler.listToTuple(
                likeItems,
                item -> {
                    results.add(item.getItemId());
                    return item.getItemId();
                },
                item -> (double) item.getMtime().getTime()
        );
        RedisClient.zSetAll(
                key,
                tuples,
                TimeoutConstants.LIKE_LIST_EXPIRE,
                TimeUnit.MILLISECONDS
        );
        return results;
    }

    @Override
    public List<Integer> loadCollectList(int userId) {
        String key = RedisConstants.USER_COLLECT_LIST + userId;
        // 全部加载出来, 后续重构(只加载部分)的话, 需要重构收藏状态判断逻辑, 目前是基于zset判断的
        List<ArticleCollect> collectList = collectRepository.listUserCollects(userId);
        if (CollectionUtils.isEmpty(collectList)) {
            return Collections.emptyList();
        }
        ArrayList<Integer> results = new ArrayList<>(collectList.size());
        Collection<Tuple> tuples = ListMapperHandler.listToTuple(
                collectList,
                item -> {
                    results.add(item.getArticleId());
                    return item.getArticleId();
                },
                item -> (double) item.getMtime().getTime()
        );
        RedisClient.zSetAll(
                key,
                tuples,
                TimeoutConstants.COLLECT_LIST_EXPIRE,
                TimeUnit.MILLISECONDS
        );
        return results;
    }

    @Override
    public List<Integer> loadFollowList(int userId) {
        String key = RedisConstants.USER_FOLLOWING_LIST + userId;
        // 全部加载出来, 后续重构(只加载部分)的话, 需要重构关注状态判断逻辑, 目前是基于zset判断的
        List<UserFollow> followList = followRepository.listUserFollowings(userId);
        if (CollectionUtils.isEmpty(followList)) {
            return Collections.emptyList();
        }
        ArrayList<Integer> results = new ArrayList<>(followList.size());
        Collection<Tuple> tuples = ListMapperHandler.listToTuple(
                followList,
                item -> {
                    results.add(item.getFollowing());
                    return item.getFollowing();
                },
                item -> (double) item.getMtime().getTime()
        );
        RedisClient.zSetAll(
                key,
                tuples,
                TimeoutConstants.FOLLOWING_LIST_EXPIRE,
                TimeUnit.MILLISECONDS
        );
        return results;
    }

    @Override
    public List<Integer> loadFollowerList(int userId) {
        String key = RedisConstants.USER_FOLLOWER_LIST + userId;
        // 加载部分
        List<UserFollow> followerList = followRepository.listUserFollowers(userId);
        if (CollectionUtils.isEmpty(followerList)) {
            return Collections.emptyList();
        }
        ArrayList<Integer> results = new ArrayList<>(followerList.size());
        Collection<Tuple> tuples = ListMapperHandler.listToTuple(
                followerList,
                item -> {
                    results.add(item.getUserId());
                    return item.getUserId();
                },
                item -> (double) item.getMtime().getTime()
        );
        RedisClient.zSetAll(
                key,
                tuples,
                TimeoutConstants.FOLLOWER_LIST_EXPIRE,
                TimeUnit.MILLISECONDS
        );
        return results;
    }

    private Integer getItemUser(int itemId, LikeType likeType) {
        Integer itemUser = null;
        if (LikeType.ARTICLE.equals(likeType)) {
            Article article = articleCacheService.getArticleInfoCache(itemId);
            itemUser = article == null ? null : article.getUserId();
        } else if (LikeType.COMMENT.equals(likeType)) {
            itemUser = commentRepository.getCommentUser(itemId);
        }
        return itemUser;
    }

    private Integer getInteractCount(int userId, String interactKey) {
        final String hash = RedisConstants.USER_INTERACT_COUNT_HASH + userId;
        return cacheService.getMapValue(
                hash,
                interactKey,
                () -> loadInteractCount(userId),
                TimeoutConstants.INTERACT_COUNT_EXPIRE
        );
    }

    private Map<String, Integer> loadInteractCount(int userId) {
        ExecutorTemplate template = new ExecutorTemplate(taskExecutor.getThreadPoolExecutor());
        try {
            long start = System.currentTimeMillis();
            Map<String, Integer> result = new ConcurrentHashMap<>();
            template.submit(() -> {
                for (LikeType type : LikeType.values()) {
                    Integer count = likeRepository.queryUserLikeItemCount(userId, type);
                    result.put(RedisConstants.LIKE_INTERACT_HKEY + type.getType(), count);
                }
            });
            template.submit(() -> {
                Integer collectCount = collectRepository.getUserCollectCount(userId);
                result.put(RedisConstants.COLLECT_INTERACT_HKEY, collectCount);
            });
            template.submit(() -> {
                Integer followCount = followRepository.getUserFollowingCount(userId);
                result.put(RedisConstants.FOLLOW_INTERACT_HKEY, followCount);
            });
            template.submit(() -> {
                Integer followerCount = followRepository.getUserFollowerCount(userId);
                result.put(RedisConstants.FOLLOWER_INTERACT_HKEY, followerCount);
            });
            template.waitForResult();
            long end = System.currentTimeMillis();
            if (log.isDebugEnabled()) {
                log.debug("get interact-count cost: {}ms", end - start);
            }
            return result;
        } catch (Exception e) {
            throw new HamiServiceException(e.getMessage(), e);
        } finally {
            template.clear();
        }
    }

    private void checkUserId(Integer itemId) {
        User cache = userCacheService.getUserCache(itemId);
        if (cache == null || cache.getUserId() == null || Objects.equals(Constants.DELETED, cache.getDeleted())) {
            throw new HamiServiceException("用户不存在");
        }
    }
}
