package top.wang3.hami.core.service.like;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.model.Like;

public interface LikeService extends IService<Like> {
    Long getUserLikes(int likerId, Integer itemType);
}
