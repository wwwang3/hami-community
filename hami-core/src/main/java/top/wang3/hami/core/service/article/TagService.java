package top.wang3.hami.core.service.article;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.TagDTO;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.model.Tag;

import java.util.Collection;
import java.util.List;

public interface TagService extends IService<Tag> {

    List<Tag>  getAllTags();

    PageData<Tag> getTagByPage(PageParam pageParam);

    List<TagDTO> getTagByIds(Collection<Integer> tagIds);
}
