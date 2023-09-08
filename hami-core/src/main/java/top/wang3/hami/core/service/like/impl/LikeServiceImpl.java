package top.wang3.hami.core.service.like.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.LikeItem;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.core.mapper.LikeMapper;
import top.wang3.hami.core.service.like.LikeService;

import java.util.List;


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
    public Long getItemLikeCount(int itemId, byte itemType) {
        return ChainWrappers.queryChain(getBaseMapper())
                .eq("item_id", itemId)
                .eq("item_type", itemType)
                .eq("`state`", Constants.ONE)
                .count();
    }

    @Override
    public List<Integer> getUserLikeArticles(Page<LikeItem> page, int userId) {
        List<LikeItem> items = ChainWrappers.queryChain(getBaseMapper())
                .select("item_id")
                .eq("liker_id", userId)
                .eq("item_type", Constants.LIKE_TYPE_ARTICLE)
                .eq("`state`", Constants.ONE)
                .list(page);
        return ListMapperHandler.listTo(items, LikeItem::getItemId);
    }

    @Transactional
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
                    .eq("`state`", Constants.ZERO)
                    .update();
        } else if (Constants.ONE.equals(likeItem.getState())) {
            throw new ServiceException("重复点赞");
        }
        return false;
    }

    @Transactional
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
