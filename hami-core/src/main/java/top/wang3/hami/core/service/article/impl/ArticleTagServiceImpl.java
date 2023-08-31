package top.wang3.hami.core.service.article.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import top.wang3.hami.common.model.ArticleTag;
import top.wang3.hami.core.mapper.ArticleTagMapper;
import top.wang3.hami.core.service.article.ArticleTagService;

import java.util.ArrayList;
import java.util.List;

@Service
public class ArticleTagServiceImpl extends ServiceImpl<ArticleTagMapper, ArticleTag>
        implements ArticleTagService {

    @Resource
    TransactionTemplate transactionTemplate;

    @Transactional
    @Override
    public void updateTags(Integer articleId, List<Integer> newTags) {
        List<ArticleTag> oldTags = listArticleTags(articleId);
        List<Integer> toDeleted  = new ArrayList<>();
        oldTags.forEach(tag -> {
            if (newTags.contains(tag.getTagId())) {
                //存在了, 不需要再添加了
                newTags.remove(tag.getTagId());
            } else {
                //不存在
                toDeleted.add(tag.getId());
            }
        });

        transactionTemplate.execute(status -> {
            if (!toDeleted.isEmpty()) {
                super.removeBatchByIds(toDeleted);
            }
            if (!newTags.isEmpty()) {
                batchSave(articleId, newTags);
            }
            return null;
        });

    }

    @Override
    @Transactional
    public void saveTags(Integer articleId, List<Integer> tagIds) {
        batchSave(articleId, tagIds);
    }

    @Transactional
    public void batchSave(Integer articleId, List<Integer> tags) {
        List<ArticleTag> tagList = tags
                .stream()
                .map(tagId -> new ArticleTag(articleId, tagId))
                .toList();
        saveBatch(tagList);
    }

    public List<ArticleTag> listArticleTags(Integer articleId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .eq("article_id", articleId)
                .list();
    }
}
