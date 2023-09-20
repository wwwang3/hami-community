package top.wang3.hami.core.service.article.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.article.TagDTO;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.model.Tag;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.repository.TagRepository;
import top.wang3.hami.core.service.article.TagService;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    TagService self;


    @Autowired
    @Lazy
    public void setSelf(TagService self) {
        this.self = self;
    }

    @Cacheable(cacheNames = Constants.CAFFEINE_CACHE_NAME, key = "'TAG_LIST'", cacheManager = Constants.CaffeineCacheManager)
    @Override
    public List<Tag> getAllTags() {
        return tagRepository.getAllTags();
    }

    @Override
    public PageData<Tag> getTagByPage(PageParam pageParam) {
        Page<Tag> page = pageParam.toPage();
        List<Tag> tags = tagRepository.getTagsByPage(page);
        page.setRecords(tags);
        return PageData.build(page);
    }

    @Override
    public List<TagDTO> getTagDTOsByIds(List<Integer> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) return Collections.emptyList();
        return ListMapperHandler.listTo(tagIds, id -> {
//            log.debug("tag_id: {}", id);
            Tag tag = self.getTagById(id);
            return new TagDTO(id, tag.getName());
        });
    }

    @Override
    public List<Tag> getTagsByIds(List<Integer> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) return Collections.emptyList();
        return ListMapperHandler.listTo(tagIds, id -> self.getTagById(id));
    }

    @Cacheable(cacheNames = Constants.CAFFEINE_CACHE_NAME, key = "'tag'+#id",
            cacheManager = Constants.CaffeineCacheManager)
    @Override
    public Tag getTagById(Integer id) {
        return tagRepository.getTagById(id);
    }

    @Override
    public boolean checkTags(List<Integer> tagIds) {
        for (Integer tagId : tagIds) {
            if (self.getTagById(tagId) == null) {
                return false;
            }
        }
        return true;
    }

}
