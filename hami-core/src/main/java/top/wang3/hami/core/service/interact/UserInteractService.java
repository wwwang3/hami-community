package top.wang3.hami.core.service.interact;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import top.wang3.hami.common.dto.interact.LikeType;
import top.wang3.hami.common.model.ArticleCollect;
import top.wang3.hami.common.model.LikeItem;
import top.wang3.hami.common.model.UserFollow;

import java.util.List;

public interface UserInteractService {

    boolean likeAction(int userId, int itemId, LikeType likeType, boolean state);

    boolean collectAction(int userId, int itemId, boolean state);

    boolean followAction(int userId, int followingId, boolean state);

    int getLikeCount(int userId, LikeType likeType);

    int getCollectCount(int userId);

    int getFollowCount(int userId);

    int getFollowerCount(int userId);

    List<Integer> getLikeList(Page<LikeItem> page, int userId, LikeType likeType);

    List<Integer> getCollectList(Page<ArticleCollect> page, int userId);

    List<Integer> getFollowList(Page<UserFollow> page, int userId);

    List<Integer> getFollowerList(Page<UserFollow> page, int userId);

    List<Integer> loadLikeList(int userId, LikeType likeType);

    List<Integer> loadCollectList(int userId);

    List<Integer> loadFollowList(int userId);

    List<Integer> loadFollowerList(int userId);

}
