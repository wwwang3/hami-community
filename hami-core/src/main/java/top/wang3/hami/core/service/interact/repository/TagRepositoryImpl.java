package top.wang3.hami.core.service.interact.repository;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.Tag;
import top.wang3.hami.core.mapper.TagMapper;

import java.util.List;

@Repository
public class TagRepositoryImpl extends ServiceImpl<TagMapper, Tag>
        implements TagRepository {

    @Override
    @Cacheable(cacheNames = Constants.CAFFEINE_CACHE_NAME, key = "'TAG_LIST'", cacheManager = Constants.CaffeineCacheManager)
    public List<Tag> getAllTags() {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("id", "name")
                .orderByAsc("id")
                .list();
    }

    @Override
    @Cacheable(cacheNames = Constants.CAFFEINE_CACHE_NAME, key = "'TAG_'+#id", cacheManager = Constants.CaffeineCacheManager)
    public Tag getTagById(Integer id) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("id", "name")
                .eq("id", id)
                .one();
    }

}
