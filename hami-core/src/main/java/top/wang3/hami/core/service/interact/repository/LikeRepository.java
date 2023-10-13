package top.wang3.hami.core.service.interact.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.enums.LikeType;
import top.wang3.hami.common.model.LikeItem;

import java.util.List;
import java.util.Map;


public interface LikeRepository extends IService<LikeItem> {


    @Transactional(rollbackFor = Exception.class)
    boolean doLike(Integer likerId, Integer itemId, LikeType likeType);

    @Transactional(rollbackFor = Exception.class)
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
    Long queryUserLikeItemCount(Integer userId, LikeType type);

    boolean hasLiked(Integer userId, Integer itemId, LikeType type);

    Map<Integer, Boolean> hasLiked(Integer userId, List<Integer> items, LikeType type);

}
