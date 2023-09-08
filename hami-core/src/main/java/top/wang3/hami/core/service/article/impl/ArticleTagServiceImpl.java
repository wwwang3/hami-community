package top.wang3.hami.core.service.article.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.converter.ArticleConverter;
import top.wang3.hami.common.dto.ArticleTagDTO;
import top.wang3.hami.common.dto.TagDTO;
import top.wang3.hami.common.model.ArticleTag;
import top.wang3.hami.common.model.Tag;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.mapper.ArticleTagMapper;
import top.wang3.hami.core.service.article.ArticleTagService;
import top.wang3.hami.core.service.article.TagService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleTagServiceImpl extends ServiceImpl<ArticleTagMapper, ArticleTag>
        implements ArticleTagService {

    @Resource
    TransactionTemplate transactionTemplate;

    private final TagService tagService;

    @Override
    public List<ArticleTagDTO> listArticleTagByArticleIds(List<Integer> articleIds) {
        if (CollectionUtils.isEmpty(articleIds)) {
            return Collections.emptyList();
        }
        return getBaseMapper().getArticleTagByArticleIds(articleIds);
    }

    @Override
    public List<TagDTO> getArticleTagByArticleId(int articleId) {
         List<ArticleTag> tags = ChainWrappers.queryChain(getBaseMapper())
                 .select("tag_id") //todo 直接冗余tag_name 修改标签名直接新增一个标签即可
                .eq("article_id", articleId)
                .list();
        return ListMapperHandler.listTo(tags, articleTag -> {
            Tag tag = tagService.getTagById(articleTag.getTagId());
            return ArticleConverter.INSTANCE.toTagDTO(tag);
        });
    }

    public List<ArticleTag> listArticleTags(Integer articleId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .eq("article_id", articleId)
                .list();
    }

    @Transactional
    @Override
    public void updateTags(Integer articleId, List<Integer> newTags) {
        List<ArticleTag> oldTags = listArticleTags(articleId);
        List<Integer> toDelete = new ArrayList<>();
        oldTags.forEach(tag -> {
            if (newTags.contains(tag.getTagId())) {
                //存在了, 不需要再添加了
                newTags.remove(tag.getTagId());
            } else {
                //不存在
                toDelete.add(tag.getId());
            }
        });

        transactionTemplate.execute(status -> {
            if (!toDelete.isEmpty()) {
                super.getBaseMapper().deleteBatchIds(toDelete);
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
}
