package top.wang3.hami.core.service.interact.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.dto.interact.LikeType;
import top.wang3.hami.common.model.LikeItem;

import java.util.List;
import java.util.Map;


public interface LikeRepository extends IService<LikeItem> {


    boolean like(Integer userId, Integer itemId, LikeType likeType, byte state);

    boolean doLike(Integer likerId, Integer itemId, LikeType likeType);

    boolean cancelLike(Integer likerId, Integer itemId, LikeType likeType);

    @Transactional(rollbackFor = Exception.class)
    int deleteLikeItem(Integer itemId, LikeType likeType);

    List<LikeItem> listUserLikeItem(Integer likerId, LikeType likeType);

    List<Integer> listUserLikeItem(Page<LikeItem> page, Integer userId, LikeType likeType);

    /**
     * 获取用户点赞的item数
     *
     * @param userId 用户ID
     * @param type   实体类型
     * @return 用户点赞的实体数
     */
    @NonNull
    Integer queryUserLikeItemCount(Integer userId, LikeType type);

    boolean hasLiked(Integer userId, Integer itemId, LikeType type);

    Map<Integer, Boolean> hasLiked(Integer userId, List<Integer> items, LikeType type);

}
