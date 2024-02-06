package top.wang3.hami.core.service.interact.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.interact.LikeType;
import top.wang3.hami.common.model.LikeItem;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.exception.HamiServiceException;
import top.wang3.hami.core.mapper.LikeMapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class LikerRepositoryImpl extends ServiceImpl<LikeMapper, LikeItem>
        implements LikeRepository {

    private static final String[] FIELDS = {"id", "item_id", "item_type", "liker_id", "`state`"};
    private static final String[] FULL_FIELDS = {"id", "item_id", "item_type", "liker_id", "`state`", "ctime", "mtime"};

    public static final String[] FIELDS_2 = {"liker_id", "item_id", "item_type", "`state`"};

    @Override
    public boolean like(Integer userId, Integer itemId, LikeType likeType, byte state) {
        byte itemType = likeType.getType();
        LikeItem likeItem = ChainWrappers.queryChain(getBaseMapper())
                .select(FIELDS_2)
                .eq("liker_id", userId)
                .eq("item_id", itemId)
                .eq("item_type", itemType)
                .one();
        if (likeItem == null && state == Constants.ONE) {
            // 插入
            LikeItem item = new LikeItem(
                    null,
                    userId,
                    itemId,
                    itemType,
                    state,
                    null,
                    null
            );
            return super.save(item);
        } else if (likeItem != null && !Objects.equals(state, likeItem.getState())) {
            // 更新
            return ChainWrappers.updateChain(getBaseMapper())
                    .set("state", state)
                    .eq("liker_id", userId)
                    .eq("item_id", itemId)
                    .eq("item_type", itemType)
                    .update();
        }
        return false;
    }

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
        } else if (Constants.ZERO.equals(likeItem.getState())) {
            //修改
            return ChainWrappers.updateChain(getBaseMapper())
                    .set("`state`", Constants.ONE)
                    .eq("id", likeItem.getId())
                    .eq("`state`", Constants.ZERO)
                    .update();
        } else {
            throw new HamiServiceException("重复点赞");
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
    @Transactional(rollbackFor = Exception.class)
    public int deleteLikeItem(Integer itemId, LikeType likeType) {
        var wrapper = Wrappers.update(new LikeItem())
                .set("state", 0)
                .eq("item_id", itemId)
                .eq("item_type", likeType.getType());
        return getBaseMapper().update(wrapper);
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
    public Integer queryUserLikeItemCount(Integer userId, LikeType likeType) {
        return ChainWrappers.queryChain(getBaseMapper())
                .eq("item_type", likeType.getType())
                .eq("liker_id", userId)
                .eq("`state`", Constants.ONE)
                .count().intValue();
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
