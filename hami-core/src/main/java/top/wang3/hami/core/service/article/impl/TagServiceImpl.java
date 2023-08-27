package top.wang3.hami.core.service.article.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.model.Tag;
import top.wang3.hami.core.mapper.TagMapper;
import top.wang3.hami.core.service.article.TagService;

import java.util.List;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
        implements TagService {


    public List<Tag> getAllTags() {
        return super.list();
    }

    @Override
    public PageData<Tag> getTagByPage(PageParam pageParam) {
        Page<Tag> page = pageParam.toPage();
        List<Tag> tags = ChainWrappers.queryChain(getBaseMapper())
                .list(page);
        page.setRecords(tags);
        return PageData.build(page);
    }

}
