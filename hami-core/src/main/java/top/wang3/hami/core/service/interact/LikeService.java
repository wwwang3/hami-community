package top.wang3.hami.core.service.interact;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import top.wang3.hami.common.dto.interact.LikeType;
import top.wang3.hami.common.model.LikeItem;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface LikeService {

    boolean doLike(Integer itemId, LikeType likeType);

    boolean cancelLike(Integer itemId, LikeType likeType);

    Long getUserLikeCount(Integer userId, LikeType likeType);

    Collection<Integer> listUserLikeArticles(Page<LikeItem> page, Integer userId);

    boolean hasLiked(Integer userId, Integer itemId, LikeType likeType);

    Map<Integer, Boolean> hasLiked(Integer userId, List<Integer> itemIds, LikeType likeType);

    Collection<Integer> loadUserLikeItem(String key, Integer userId, LikeType likeType, long current, long size);
}
