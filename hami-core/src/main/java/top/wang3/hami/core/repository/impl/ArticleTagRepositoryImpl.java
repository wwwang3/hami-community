package top.wang3.hami.core.repository.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.model.ArticleTag;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.mapper.ArticleTagMapper;
import top.wang3.hami.core.repository.ArticleTagRepository;

import java.util.List;

@Repository
public class ArticleTagRepositoryImpl extends ServiceImpl<ArticleTagMapper, ArticleTag>
        implements ArticleTagRepository {

    @Override
    public List<ArticleTag> getArticleTagsById(Integer articleId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("article_id", "tag_id")
                .eq("article_id", articleId)
                .list();
    }

    @Override
    public List<Integer> getArticleTagIdsById(Integer articleId) {
        List<ArticleTag> tags = getArticleTagsById(articleId);
        return ListMapperHandler.listTo(tags, ArticleTag::getTagId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveArticleTags(final Integer articleId, List<Integer> tagIds) {
        List<ArticleTag> tags = ListMapperHandler.listTo(tagIds, (tagId) -> new ArticleTag(articleId, tagId));
        return super.saveBatch(tags);
    }

    @Override
    public boolean deleteArticleTags(List<Integer> ids) {
        return super.removeBatchByIds(ids);
    }
}
