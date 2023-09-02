package top.wang3.hami.core.service.user;

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


}
