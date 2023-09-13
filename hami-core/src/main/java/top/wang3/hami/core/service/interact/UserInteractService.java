package top.wang3.hami.core.service.interact;

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

    boolean hasFollowed(int userId, int followingId);

    Map<Integer, Boolean> hasFollowed(int userId, List<Integer> followings);

    boolean hasLiked(int userId, int itemId, byte itemType);

    Map<Integer, Boolean> hasLiked(int userId, List<Integer> itemId, byte itemType);

    boolean hasCollected(int userId, int itemId, byte itemType);
    Map<Integer, Boolean> hasCollected(int userId, List<Integer> itemId, byte itemType);

    Integer getUserLikes(Integer userId);

    Integer getUserCollects(Integer userId);

    Integer getUserFollowings(Integer userId);

    Integer getUserFollowers(Integer userId);;
}
