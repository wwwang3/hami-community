package top.wang3.hami.core.service.article.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.model.Tag;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.service.article.TagService;
import top.wang3.hami.core.service.interact.repository.TagRepository;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;


    @Override
    public List<Tag> getAllTag() {
        return tagRepository.getAllTags();
    }

    @Override
    public PageData<Tag> getTagByPage(PageParam pageParam) {
        List<Tag> tags = tagRepository.getAllTags();
        List<Tag> records = ListMapperHandler.subList(tags, pageParam.getCurrent(), pageParam.getSize());
        return new PageData<>(pageParam.getCurrent(), tags.size(), records);
    }

    @Override
    public List<Tag> getTagsByIds(List<Integer> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) return Collections.emptyList();
        return ListMapperHandler.listTo(tagIds, this::getTagById, false);
    }

    @Override
    public Tag getTagById(Integer id) {
        return tagRepository.getTagById(id);
    }

}
