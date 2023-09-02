package top.wang3.hami.core.service.like;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.model.LikeItem;

public interface LikeService extends IService<LikeItem> {
    Long getUserLikeCount(int likerId, byte itemType);

    boolean doLike(int likerId, int itemId, byte itemType);

    boolean cancelLike(int likerId, int itemId, byte itemType);
}
