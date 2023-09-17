package top.wang3.hami.core.service.interact;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import top.wang3.hami.common.dto.request.CommentParam;
import top.wang3.hami.common.model.ArticleCollect;
import top.wang3.hami.common.model.Comment;
import top.wang3.hami.common.model.LikeItem;
import top.wang3.hami.common.model.UserFollow;

import java.util.List;
import java.util.Map;

public interface UserInteractService {

    //关注
    boolean follow(int followingId);
    boolean unFollow(int followingId);

    //点赞
    boolean like(int itemId, byte type);
    boolean cancelLike(int itemId, byte type);

    //收藏
    boolean collect(int articleId);
    boolean cancelCollect(int articleId);

    Comment publishComment(CommentParam param);

    Comment publishReply(CommentParam param);

    boolean deleteComment(Integer id);

    boolean hasFollowed(int userId, int followingId);

    Map<Integer, Boolean> hasFollowed(int userId, List<Integer> followings);

    boolean hasLiked(int userId, int itemId, byte itemType);

    Map<Integer, Boolean> hasLiked(int userId, List<Integer> itemId, byte itemType);

    boolean hasCollected(int userId, int itemId, byte itemType);
    Map<Integer, Boolean> hasCollected(int userId, List<Integer> itemId, byte itemType);

    Integer getUserLikeCount(Integer userId);

    Integer getUserCollectCount(Integer userId);

    Integer getUserFollowingCount(Integer userId);

    Integer getUserFollowerCount(Integer userId);

    List<Integer> getUserCollectArticles(Page<ArticleCollect> page, Integer userId);

    List<Integer> getUserLikesArticles(Page<LikeItem> page, Integer userId);

    List<Integer> getUserFollowings(Page<UserFollow> page, Integer userId);

    List<Integer> getUserFollowers(Page<UserFollow> page, Integer userId);
}
