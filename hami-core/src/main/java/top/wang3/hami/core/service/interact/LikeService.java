package top.wang3.hami.core.service.interact;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import top.wang3.hami.common.enums.LikeType;
import top.wang3.hami.common.model.LikeItem;

import java.util.List;
import java.util.Map;

public interface LikeService {

    boolean doLike(Integer itemId, LikeType likeType);

    boolean cancelLike(Integer itemId, LikeType likeType);

    Long getUserLikeCount(Integer userId, LikeType likeType);

    List<Integer> getUserLikeArticles(Page<LikeItem> page, Integer userId);

    boolean hasLiked(Integer userId, Integer itemId, LikeType likeType);

    Map<Integer, Boolean> hasLiked(Integer userId, List<Integer> itemId, LikeType likeType);

    List<Integer> loadUserLikeArticleCache(String key, Integer userId, long current, long size);
}
