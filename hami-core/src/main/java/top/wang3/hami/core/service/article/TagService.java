package top.wang3.hami.core.service.article;

import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.model.Tag;

import java.util.List;

public interface TagService {

    List<Tag> getAllTag();

    PageData<Tag> getTagByPage(PageParam pageParam);

    List<Tag> getTagsByIds(List<Integer> tagIds);

    Tag getTagById(Integer id);

}
