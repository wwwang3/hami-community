package top.wang3.hami.core.service.interact.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.enums.LikeType;
import top.wang3.hami.common.model.LikeItem;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.core.mapper.LikeMapper;

import java.util.List;
import java.util.Map;

@Repository
public class LikerRepositoryImpl extends ServiceImpl<LikeMapper, LikeItem>
        implements LikeRepository {

    private static final String[] FIELDS = {"id", "item_id", "item_type", "liker_id", "`state`"};
    private static final String[] FULL_FIELDS = {"id", "item_id", "item_type", "liker_id", "`state`", "ctime", "mtime"};


    @Override
    public boolean doLike(Integer likerId, Integer itemId, LikeType likeType) {
        Byte itemType = likeType.getType();
        LikeItem likeItem = ChainWrappers.queryChain(getBaseMapper())
                .select(FIELDS)
                .eq("liker_id", likerId)
                .eq("item_id", itemId)
                .eq("item_type", itemType)
                .one();
        if (likeItem == null) {
            //新增
            LikeItem item = new LikeItem(likerId, itemId, itemType);
            item.setState(Constants.ONE);
            return super.save(item);
        } else if (Constants.ZERO.equals(likeItem.getItemType())){
            //修改
            return ChainWrappers.updateChain(getBaseMapper())
                    .set("`state`", Constants.ONE)
                    .eq("id", likeItem.getId())
                    .eq("`state`", Constants.ONE)
                    .update();
        } else {
            throw new ServiceException("重复点赞");
        }
    }

    @Override
    public boolean cancelLike(Integer likerId, Integer itemId, LikeType likeType) {
        return ChainWrappers.updateChain(getBaseMapper())
                .set("`state`", Constants.ZERO)
                .eq("liker_id", likerId)
                .eq("item_id", itemId)
                .eq("item_type", likeType.getType())
                .eq("`state`", Constants.ONE)
                .update();
    }

    @Override
    public int deleteLikeItem(Integer itemId, LikeType likeType) {
        var wrapper = Wrappers.update(new LikeItem())
                .eq("item_id", itemId)
                .eq("item_type", likeType.getType());
        return getBaseMapper().delete(wrapper);
    }

    @Override
    public List<LikeItem> listUserLikeItem(Integer likerId, LikeType likeType) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select(FULL_FIELDS)
                .eq("item_type", likeType.getType())
                .eq("liker_id", likerId)
                .eq("`state`", Constants.ONE)
                .orderByDesc("mtime") //最近点赞的文章
                .list();
    }

    @Override
    public List<Integer> listUserLikeItem(Page<LikeItem> page, Integer userId, LikeType likeType) {
        List<LikeItem> items = ChainWrappers.queryChain(getBaseMapper())
                .select("item_id")
                .eq("item_type", likeType.getType())
                .eq("liker_id", userId)
                .eq("`state`", Constants.ONE)
                .orderByDesc("mtime") //最近点赞的文章
                .list(page);
        return ListMapperHandler.listTo(items, LikeItem::getItemId);
    }

    @NonNull
    @Override
    public Long queryUserLikeItemCount(Integer userId, LikeType likeType) {
        return ChainWrappers.queryChain(getBaseMapper())
                .eq("item_type", likeType.getType())
                .eq("liker_id", userId)
                .eq("`state`", Constants.ONE)
                .count();
    }

    @Override
    public boolean hasLiked(Integer userId, Integer itemId, LikeType type) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("item_id")
                .eq("item_id", itemId)
                .eq("item_type", type.getType())
                .eq("liker_id", userId)
                .eq("`state`", Constants.ONE)
                .exists();
    }

    @Override
    public Map<Integer, Boolean> hasLiked(Integer userId, List<Integer> items, LikeType type) {
        List<LikeItem> liked = ChainWrappers.queryChain(getBaseMapper())
                .select("item_id", "state")
                .in("item_id", items)
                .eq("item_type", type.getType())
                .eq("liker_id", userId)
                .eq("state", Constants.ONE)
                .list();
        return ListMapperHandler.listToMap(liked, LikeItem::getItemId, i -> Boolean.TRUE);
    }
}
