package top.wang3.hami.core.service.interact.repository;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Repository;
import top.wang3.hami.common.model.Tag;
import top.wang3.hami.core.mapper.TagMapper;

import java.util.List;

@Repository
public class TagRepositoryImpl extends ServiceImpl<TagMapper, Tag>
        implements TagRepository {

    @Override
    public List<Tag> getAllTags() {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("id", "name")
                .list();
    }

    @Override
    public Tag getTagById(Integer id) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("id", "name")
                .eq("id", id)
                .one();
    }

    @Override
    public List<Tag> getTagByIds(List<Integer> ids) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("id", "name")
                .in("id", ids)
                .list();
    }

    @Override
    public List<Tag> getTagsByPage(Page<Tag> page) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("id", "name")
                .list(page);
    }
}
