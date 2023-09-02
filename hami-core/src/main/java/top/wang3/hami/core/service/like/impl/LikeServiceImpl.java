package top.wang3.hami.core.service.like.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.LikeItem;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.core.mapper.LikeMapper;
import top.wang3.hami.core.service.like.LikeService;


@Service
public class LikeServiceImpl extends ServiceImpl<LikeMapper, LikeItem>
        implements LikeService {

    @Override
    public Long getUserLikeCount(int likerId, byte itemType) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("liker_id")
                .eq("liker_id", likerId)
                .eq("item_type", itemType)
                .eq("`state`", Constants.ONE)
                .count();
    }

    @Override
    public boolean doLike(int likerId, int itemId, byte itemType) {
        LikeItem likeItem = ChainWrappers.queryChain(getBaseMapper())
                .eq("liker_id", likerId)
                .eq("item_id", itemId)
                .eq("item_type", itemType)
                .one();
        if (likeItem == null) {
            LikeItem item = new LikeItem(itemId, itemType, likerId);
            return super.save(item);
        }  else if (Constants.ZERO.equals(likeItem.getState())) {
            //更新
            return ChainWrappers.updateChain(getBaseMapper())
                    .set("`state`", Constants.ONE)
                    .eq("item_id", itemId)
                    .eq("item_type", itemType)
                    .eq("liker_id", likerId)
                    .update();
        } else if (Constants.ONE.equals(likeItem.getState())) {
            throw new ServiceException("重复点赞");
        }
        return false;
    }

    @Override
    public boolean cancelLike(int likerId, int itemId, byte itemType) {
        return ChainWrappers.updateChain(getBaseMapper())
                .set("`state`", Constants.ZERO)
                .eq("item_id", itemId)
                .eq("item_type", itemType)
                .eq("liker_id", likerId)
                .eq("`state`", Constants.ONE)
                .update();
    }
}
