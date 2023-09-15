package top.wang3.hami.core.service.article;

import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.TagDTO;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.model.Tag;

import java.util.List;

public interface TagService {

    List<Tag>  getAllTags();

    PageData<Tag> getTagByPage(PageParam pageParam);

    List<TagDTO> getTagDTOsByIds(List<Integer> tagIds);

    List<Tag> getTagsByIds(List<Integer> tagIds);

    Tag getTagById(Integer id);

    boolean checkTags(List<Integer> tagIds);
}
