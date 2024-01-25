package top.wang3.hami.core.service.article.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import java.util.Map;

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
    public Map<Integer, Tag> getTagMap() {
        List<Tag> tags = tagRepository.getAllTags();
        return ListMapperHandler.listToMap(tags, Tag::getId);
    }

    @Override
    public PageData<Tag> getTagByPage(PageParam pageParam) {
        Page<Tag> page = pageParam.toPage();
        List<Tag> tags = tagRepository.getAllTags();
        List<Tag> records = ListMapperHandler.subList(tags, page.getCurrent(), page.getSize());
        page.setRecords(records);
        return PageData.build(page);
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
