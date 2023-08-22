package top.wang3.hami.core.service.like;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.Like;
import top.wang3.hami.core.mapper.LikeMapper;


@Service
public class LikeServiceImpl extends ServiceImpl<LikeMapper, Like>
        implements LikeService {
    @Override
    public Long getUserLikes(int likerId, Integer itemType) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("liker_id")
                .eq("liker_id", likerId)
                .eq("item_type", itemType)
                .eq("`state`", Constants.ONE)
                .count();
    }
}
