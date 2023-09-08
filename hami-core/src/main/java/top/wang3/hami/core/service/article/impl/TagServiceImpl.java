package top.wang3.hami.core.service.article.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.TagDTO;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.model.Tag;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.mapper.TagMapper;
import top.wang3.hami.core.service.article.TagService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
        implements TagService {

    @Cacheable(cacheNames = Constants.CAFFEINE_CACHE_NAME, key = "'TAG_LIST'", cacheManager = Constants.CaffeineCacheManager)
    @Override
    public List<Tag> getAllTags() {
        return super.list();
    }

    @Override
    public PageData<Tag> getTagByPage(PageParam pageParam) {
        Page<Tag> page = pageParam.toPage();
        List<Tag> tags = ChainWrappers.queryChain(getBaseMapper()).list(page);
        page.setRecords(tags);
        return PageData.build(page);
    }

    @Override
    public List<TagDTO> getTagDTOsByIds(Collection<Integer> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) return Collections.emptyList();
        List<Tag> tags = super.listByIds(tagIds);
        return ListMapperHandler.listTo(tags, t -> new TagDTO(t.getId(), t.getName()));
    }

    @Override
    public List<Tag> getTagsByIds(Collection<Integer> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) return Collections.emptyList();
        return Optional.ofNullable(super.listByIds(tagIds))
                .orElse(Collections.emptyList());
    }

    @Override
    @Cacheable(cacheNames = Constants.CAFFEINE_CACHE_NAME, key = "'tag'+#id", cacheManager = Constants.CaffeineCacheManager)
    public Tag getTagById(int id) {
        return super.getById(id);
    }

}
