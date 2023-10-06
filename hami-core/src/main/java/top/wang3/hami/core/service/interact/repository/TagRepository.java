package top.wang3.hami.core.service.interact.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.model.Tag;

import java.util.List;

public interface TagRepository extends IService<Tag> {

    List<Tag> getAllTags();

    Tag getTagById(Integer id);

    List<Tag> getTagByIds(List<Integer> ids);

    List<Tag> getTagsByPage(Page<Tag> page);
}
