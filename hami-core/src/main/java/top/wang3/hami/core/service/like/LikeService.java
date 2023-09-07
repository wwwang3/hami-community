package top.wang3.hami.core.service.like;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.model.LikeItem;

import java.util.List;

public interface LikeService extends IService<LikeItem> {
    Long getUserLikeCount(int likerId, byte itemType);

    Long getItemLikeCount(int itemId, byte itemType);

    List<Integer> getUserLikeArticles(Page<LikeItem> page, int userId);

    boolean doLike(int likerId, int itemId, byte itemType);

    boolean cancelLike(int likerId, int itemId, byte itemType);
}
