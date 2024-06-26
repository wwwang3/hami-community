package top.wang3.hami.core.service.interact;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import top.wang3.hami.common.dto.interact.LikeType;
import top.wang3.hami.common.model.LikeItem;

import java.util.List;
import java.util.Map;

public interface LikeService {

    boolean doLike(Integer itemId, LikeType likeType);

    boolean cancelLike(Integer itemId, LikeType likeType);

    Integer getUserLikeCount(Integer userId, LikeType likeType);

    List<Integer> listUserLikeArticles(Page<LikeItem> page, Integer userId);

    boolean hasLiked(Integer userId, Integer itemId, LikeType likeType);

    Map<Integer, Boolean> hasLiked(Integer userId, List<Integer> itemIds, LikeType likeType);

    @SuppressWarnings("UnusedReturnValue")
    List<Integer> loadUserLikeItem(Integer userId, LikeType likeType);
}
