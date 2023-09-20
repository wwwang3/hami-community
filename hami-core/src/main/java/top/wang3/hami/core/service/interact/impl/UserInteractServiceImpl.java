package top.wang3.hami.core.service.interact.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.converter.CommentConverter;
import top.wang3.hami.common.dto.request.CommentParam;
import top.wang3.hami.common.message.*;
import top.wang3.hami.common.model.ArticleCollect;
import top.wang3.hami.common.model.Comment;
import top.wang3.hami.common.model.LikeItem;
import top.wang3.hami.common.model.UserFollow;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.component.NotifyMsgPublisher;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.core.mapper.ArticleMapper;
import top.wang3.hami.core.mapper.CommentMapper;
import top.wang3.hami.core.repository.CommentRepository;
import top.wang3.hami.core.repository.UserRepository;
import top.wang3.hami.core.service.article.ArticleCollectService;
import top.wang3.hami.core.service.article.ArticleStatService;
import top.wang3.hami.core.service.common.RabbitMessagePublisher;
import top.wang3.hami.core.service.interact.UserInteractService;
import top.wang3.hami.core.service.like.LikeService;
import top.wang3.hami.core.service.user.UserFollowService;
import top.wang3.hami.security.context.IpContext;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户行为Service实现 todo 完善
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserInteractServiceImpl implements UserInteractService {

    private final UserFollowService userFollowService;
    private final LikeService likeService;
    private final ArticleCollectService articleCollectService;
    private final NotifyMsgPublisher notifyMsgPublisher;
    private final ArticleStatService articleStatService;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    private final RabbitMessagePublisher rabbitMessagePublisher;
    @Resource
    ArticleMapper articleMapper;

    @Resource
    CommentMapper commentMapper;

    @Resource
    TransactionTemplate transactionTemplate;

    @Override
    public boolean follow(int followingId) {
        //用户关注
        //被关注用户的粉丝数+1
        //用户的关注数+1 (有Canal发送MQ消费)
        //发送关注消息
        int loginUserId = LoginUserContext.getLoginUserId();
        //check user
        checkUserExist(followingId);
        //关注
        Boolean success = transactionTemplate.execute(status -> {
            return userFollowService.follow(loginUserId, followingId);
        });
        if (Boolean.TRUE.equals(success)) {
            rabbitMessagePublisher.publishMsg(new FollowRabbitMessage(loginUserId, followingId, true));
            return true;
        }
        return false;
    }

    @Override
    public boolean unFollow(int followingId) {
        //用户取消关注
        //被关注用户的粉丝数-1
        //用户的关注数-1 (有Canal发送MQ消费)
        int loginUserId = LoginUserContext.getLoginUserId();
        boolean success = userFollowService.unFollow(loginUserId, followingId);
        if (!success) {
            return false;
        }
        rabbitMessagePublisher.publishMsg(new FollowRabbitMessage(loginUserId, followingId, false));
        return true;
    }

    @Transactional
    @Override
    public boolean like(int itemId, byte type) {
        //用户点赞
        //文章点赞数+1
        //用户的赞过+1 (有Canal发送MQ消费)
        //发送点赞消息
        int loginUserId = LoginUserContext.getLoginUserId();
        checkItemExist(itemId, type);
        boolean success = likeService.doLike(loginUserId, itemId, type);
        if (!success) return false;
        //发布点赞消息
        //存在更新失败(消费失败问题)
        rabbitMessagePublisher.publishMsg(new LikeRabbitMessage(loginUserId, itemId, type, true));
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean cancelLike(int itemId, byte type) {
        //用户取消
        //文章点赞数-1
        //用户的赞过-1 (Canal发送MQ消费,然后更新Redis)
        int loginUserId = LoginUserContext.getLoginUserId();
        boolean cancelled = likeService.cancelLike(loginUserId, itemId, type);
        if (!cancelled) return false;
        rabbitMessagePublisher.publishMsg(new LikeRabbitMessage(loginUserId, itemId, type, false));
        return true;
    }

    @Override
    public boolean collect(int articleId) {
        //用户收藏
        //文章收藏+1
        //用户的收藏+1 (有Canal发送MQ消费)
        //发送收藏消息
        int loginUserId = LoginUserContext.getLoginUserId();
        checkItemExist(articleId, Constants.LIKE_TYPE_ARTICLE);
        Boolean success = transactionTemplate.execute(status -> {
            return articleCollectService.collectArticle(loginUserId, articleId);
        });
        if (Boolean.FALSE.equals(success)) return false;
        rabbitMessagePublisher.publishMsg(new CollectRabbitMessage(loginUserId, articleId, true));
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean cancelCollect(int articleId) {
        int loginUserId = LoginUserContext.getLoginUserId();
        boolean success = articleCollectService.cancelCollectArticle(loginUserId, articleId);
        if (!success) return false;
        //decrease
        rabbitMessagePublisher.publishMsg(new CollectRabbitMessage(loginUserId, articleId, false));
        return true;
    }

    @Override
    public Comment publishComment(CommentParam param) {
        //发表评论
        //todo 敏感词过滤
        return publishComment(param, false);
    }

    @Override
    public Comment publishReply(CommentParam param) {
        //回复
        return publishComment(param, true);
    }

    @Override
    public boolean deleteComment(Integer id) {
        Comment comment = commentRepository.getById(id);
        int userId = LoginUserContext.getLoginUserId();
        int deleted = commentRepository.deleteComment(userId, id);
        if (deleted > 0) {
            //评论删除消息
            var message = new CommentDeletedRabbitMessage(comment.getArticleId(), deleted);
            rabbitMessagePublisher.publishMsg(message);
            //管他投递成不成功
            return true;
        }
        return false;
    }

    private Comment publishComment(CommentParam param, boolean reply) {
        Comment comment = buildComment(param, reply);
        //评论
        Boolean success = transactionTemplate.execute(status -> {
            return commentRepository.save(comment);
        });
        if (!Boolean.TRUE.equals(success)) {
            return null;
        }
        if (reply) {
            RabbitMessage message = new ReplyRabbitMessage(comment.getUserId(), comment.getArticleId(),
                    comment.getId(), comment.getParentId(), comment.getContent());
            rabbitMessagePublisher.publishMsg(message);

        } else {
            if (!comment.getIsAuthor()) {
                RabbitMessage message = new CommentRabbitMessage(comment.getUserId(), comment.getArticleId(),
                        comment.getId(), comment.getContent());
                rabbitMessagePublisher.publishMsg(message);

            }
        }
        return comment;
    }

    private Comment buildComment(CommentParam param, boolean reply) {
        int loginUserId = LoginUserContext.getLoginUserId();
        Integer author = articleMapper.getArticleAuthor(param.getArticleId());
        if (author == null) {
            throw new IllegalArgumentException("参数错误");
        }
        Comment comment = CommentConverter.INSTANCE.toComment(param);
        comment.setIsAuthor(loginUserId == author);
        comment.setUserId(loginUserId); //评论用户
        comment.setIpInfo(IpContext.getIpInfo()); //IP信息
        if (reply) {
            return buildReply(comment, param);
        }
        return comment;
    }

    private Comment buildReply(Comment comment, CommentParam param) {
        //回复, parentId和rootId必须都大于0
        //check rootId;
        //check parentId;
        //一级评论 rootId和parentId都为0
        //二级评论 rootId = parentId != 0
        Integer rootId = param.getRootId();
        Integer parentId = param.getParentId();
        Assert.isTrue(rootId != 0, "rootId can not be 0");
        Assert.isTrue(rootId != 0, "parentId can not be 0");
        Comment parentComemnt = commentRepository.getById(parentId);
        if (parentComemnt == null) {
            throw new ServiceException("参数错误");
        } else if (rootId == parentId && parentComemnt.getRootId() != 0) {
            //二级评论 回复的是根评论
            //根评论的rootId应该为0
            throw new ServiceException("参数错误");
        } else if (parentComemnt.getRootId() != rootId) {
            //三级以上的评论, 必须在同一个根评论下
            throw new ServiceException("参数错误");
        }
        comment.setRootId(rootId);
        comment.setParentId(parentId);
        //回复的是父评论的userId
        comment.setReplyTo(parentComemnt.getUserId());
        return comment;
    }

    @Override
    public boolean hasFollowed(int userId, int followingId) {
        //从Redis中获取用户的关注列表
        //当用户关注或者取关时，通过Canal监听Binlog同步到redis
        //redis中使用ZSet结构存储，Score为时间戳
        String redisKey = Constants.LIST_USER_FOLLOWING + userId;
        return RedisClient.zContains(redisKey, followingId);
    }

    @NonNull
    public Map<Integer, Boolean> hasFollowed(int userId, List<Integer> followings) {
        String redisKey = Constants.LIST_USER_FOLLOWING + userId;
        return RedisClient.zMContains(redisKey, followings);
    }

    @Override
    public boolean hasLiked(int userId, int itemId, byte itemType) {
        String redisKey = Constants.LIST_USER_LIKE + userId + ":" + itemType;
        return RedisClient.zContains(redisKey, itemId);
    }

    @CostLog
    @Override
    public Map<Integer, Boolean> hasLiked(int userId, List<Integer> items, byte itemType) {
        String redisKey = Constants.LIST_USER_LIKE + userId + ":" + itemType;
        return RedisClient.zMContains(redisKey, items);
    }

    @Override
    public boolean hasCollected(int userId, int itemId, byte itemType) {
        String redisKey = Constants.LIST_USER_COLLECT + userId;
        return RedisClient.zContains(redisKey, itemId);
    }

    @Override
    public Map<Integer, Boolean> hasCollected(int userId, List<Integer> items, byte itemType) {
        String redisKey = Constants.LIST_USER_COLLECT + userId;
        return RedisClient.zMContains(redisKey, items);
    }

    @Override
    public Integer getUserLikeCount(Integer userId) {
        //获取用户点赞的文章数
        String redisKey = Constants.LIST_USER_LIKE + userId;
        Long count = RedisClient.zCard(redisKey);
        return count != null ? count.intValue() : 0;
    }

    @Override
    public Integer getUserCollectCount(Integer userId) {
        //获取用户点赞的文章数
        String redisKey = Constants.LIST_USER_COLLECT + userId;
        Long count = RedisClient.zCard(redisKey);
        return count != null ? count.intValue() : 0;
    }

    @Override
    public Integer getUserFollowingCount(Integer userId) {
        //获取用户点赞的文章数
        String redisKey = Constants.LIST_USER_FOLLOWING + userId;
        Long count = RedisClient.zCard(redisKey);
        return count != null ? count.intValue() : 0;
    }

    @Override
    public Integer getUserFollowerCount(Integer userId) {
        //获取用户点赞的文章数
        String redisKey = Constants.LIST_USER_FOLLOWER + userId;
        Long count = RedisClient.zCard(redisKey);
        return count != null ? count.intValue() : 0;
    }

    @Override
    public List<Integer> getUserCollectArticles(Page<ArticleCollect> page, Integer userId) {
        String redisKey = Constants.LIST_USER_COLLECT + userId;
        if (!RedisClient.exist(redisKey)) {
            return loadUserCollectArticlesFromDB(page, redisKey, userId);
        }
        List<Integer> collects = RedisClient.zRevPage(redisKey, page.getCurrent(), page.getSize());
        page.setTotal(RedisClient.zCard(redisKey));
        return collects;
    }

    @Override
    public List<Integer> getUserLikesArticles(Page<LikeItem> page, Integer userId) {
        String redisKey = Constants.LIST_USER_LIKE + userId;
        if (RedisClient.exist(redisKey)) {
            List<Integer> likes = RedisClient.zRevPage(redisKey, page.getCurrent(), page.getSize());
            page.setTotal(RedisClient.zCard(redisKey));
            return likes;
        }
        return loadUserLikeArticlesFromDB(page, redisKey, userId);
    }

    @Override
    public List<Integer> getUserFollowings(Page<UserFollow> page, Integer userId) {
        String redisKey = Constants.LIST_USER_FOLLOWING + userId;
        if (RedisClient.exist(redisKey)) {
            List<Integer> followings = RedisClient.zRevPage(redisKey, page.getCurrent(), page.getSize());
            page.setTotal(RedisClient.zCard(redisKey));
            return followings;
        }
        return loadUserFollowingsFromDB(page, redisKey, userId);
    }

    @Override
    public List<Integer> getUserFollowers(Page<UserFollow> page, Integer userId) {
        String redisKey = Constants.LIST_USER_FOLLOWING + userId;
        if (!RedisClient.exist(redisKey)) {
            return loadUserFollowersFromDB(page, redisKey, userId);
        } else {
            long total = RedisClient.zCard(redisKey);
            long index = page.getCurrent() * page.getSize();
            if (total > 1000 && index > total) {
                //回源DB
                return userFollowService.getUserFollowers(page, userId);
            }
            List<Integer> followers = RedisClient.zRevPage(redisKey, page.getCurrent(), page.getSize());
            page.setTotal(total);
            return followers;
        }
    }

    private void checkItemExist(int itemId, int itemType) {
        if (itemType == Constants.LIKE_TYPE_ARTICLE) {
            if (!articleMapper.isArticleExist(itemId)) {
                throw new ServiceException("参数错误");
            }
        } else if (itemType == Constants.LIKE_TYPE_COMMENT) {
            if (!commentMapper.isCommentExist(itemId)) {
                throw new ServiceException("参数错误");
            }
        }
    }

    private void checkUserExist(int userId) {
        if (!userRepository.checkUserExist(userId)) {
            throw new IllegalArgumentException("用户不存在");
        }
    }

    private List<Integer> loadUserCollectArticlesFromDB(Page<ArticleCollect> page, String key, Integer userId) {
        //to many duplicates code
        List<ArticleCollect> collects = articleCollectService.getUserCollectArticles(userId);
        int current = (int) page.getCurrent();
        int size = (int) page.getSize();
        var tuples = ListMapperHandler.listToZSet(collects, ArticleCollect::getArticleId, a -> {
                    return (double) a.getMtime().getTime();
                }
        );
        setZsetCache(key, tuples);
        page.setTotal(Math.min(1000, collects.size()));
        return ListMapperHandler.subList(collects, ArticleCollect::getArticleId, current, size);
    }

    private List<Integer> loadUserLikeArticlesFromDB(Page<LikeItem> page, String key, Integer userId) {
        List<LikeItem> likes = likeService.getUserLikeArticles(userId);
        int current = (int) page.getCurrent();
        int size = (int) page.getSize();
        var tuples = ListMapperHandler.listToZSet(likes, LikeItem::getItemId, item -> {
            return (double) item.getMtime().getTime();
        });
        setZsetCache(key, tuples);
        page.setTotal(Math.min(1000, likes.size()));
        return ListMapperHandler.subList(likes, LikeItem::getItemId, current, size);
    }

    private List<Integer> loadUserFollowingsFromDB(Page<UserFollow> page, String key, Integer userId) {
        List<UserFollow> followings = userFollowService.getUserFollowings(userId);
        int current = (int) page.getCurrent();
        int size = (int) page.getSize();
        var set = ListMapperHandler.listToZSet(followings, UserFollow::getFollowing, item -> {
            return (double) item.getMtime().getTime();
        });
        setZsetCache(key, set);
        page.setTotal(Math.min(1000, followings.size()));
        return ListMapperHandler.subList(followings, UserFollow::getFollowing, current, size);
    }

    private List<Integer> loadUserFollowersFromDB(Page<UserFollow> page, String key, Integer userId) {
        //一般用户点赞/关注收藏的文章不会太多
        //用户的粉丝一般需要回源查询
        int current = (int) page.getCurrent();
        int size = (int) page.getSize();
        //小于1000条全部读出来放Redis
        List<UserFollow> followers = userFollowService.getUserFollowers(userId);
        var tuples = ListMapperHandler.listToZSet(followers, UserFollow::getUserId, item -> {
            return (double) item.getMtime().getTime();
        });
        setZsetCache(key, tuples);
        page.setTotal(Math.min(1000, followers.size()));
        return ListMapperHandler.subList(followers, UserFollow::getUserId, current, size);
    }


    private <T> void setZsetCache(String key, Set<ZSetOperations.TypedTuple<T>> pairs) {
        RedisClient.zAddAll(key, pairs);
    }
}
