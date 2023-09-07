package top.wang3.hami.core.service.common.impl;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.User;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.core.mapper.ArticleMapper;
import top.wang3.hami.core.mapper.CommentMapper;
import top.wang3.hami.core.mapper.UserMapper;
import top.wang3.hami.core.service.article.ArticleCollectService;
import top.wang3.hami.core.service.common.UserInteractService;
import top.wang3.hami.core.service.like.LikeService;
import top.wang3.hami.core.service.user.UserFollowService;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.List;
import java.util.Map;

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

    @Resource
    ArticleMapper articleMapper;

    @Resource
    CommentMapper commentMapper;

    @Resource
    UserMapper userMapper;

    @Override
    public boolean follow(int followingId) {
        int loginUserId = LoginUserContext.getLoginUserId();
        //check user
        checkUserExist(followingId);
        return userFollowService.follow(loginUserId, followingId);
    }

    @Override
    public boolean unFollow(int followingId) {
        int loginUserId = LoginUserContext.getLoginUserId();
        return userFollowService.unFollow(loginUserId, followingId);
    }

    @Override
    public boolean like(int itemId, byte type) {
        int loginUserId = LoginUserContext.getLoginUserId();
        checkItemExist(itemId, type);
        //todo 更新文章点赞数
        return likeService.doLike(loginUserId, itemId, type);
    }

    @Override
    public boolean cancelLike(int itemId, byte type) {
        int loginUserId = LoginUserContext.getLoginUserId();
        return likeService.cancelLike(loginUserId, itemId, type);
    }

    @Override
    public boolean collect(int articleId) {
        int loginUserId = LoginUserContext.getLoginUserId();
        checkItemExist(articleId, Constants.LIKE_TYPE_ARTICLE);
        //todo 更新文章收藏数
        return articleCollectService.collectArticle(loginUserId, articleId);
    }

    @Override
    public boolean cancelCollect(int userId) {
        return false;
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
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("参数错误");
        }
    }
}
