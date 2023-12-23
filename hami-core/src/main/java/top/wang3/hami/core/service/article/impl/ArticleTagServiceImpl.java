package top.wang3.hami.core.service.article.impl;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import top.wang3.hami.common.model.ArticleTag;
import top.wang3.hami.core.service.article.ArticleTagService;
import top.wang3.hami.core.service.article.repository.ArticleTagRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleTagServiceImpl implements ArticleTagService {

    @Resource
    TransactionTemplate transactionTemplate;

    private final ArticleTagRepository articleTagRepository;


    @Override
    public boolean updateTags(Integer articleId, List<Integer> newTags) {
        List<ArticleTag> oldTags = articleTagRepository.getArticleTagsById(articleId);
        List<Integer> toDelete = new ArrayList<>();
        // [1,2,3] [2,1,4]
        oldTags.forEach(tag -> {
            if (newTags.contains(tag.getTagId())) {
                //存在了, 不需要再添加了
                newTags.remove(tag.getTagId());
            } else {
                //在旧的里面, 不在新的里面
                toDelete.add(tag.getId());
            }
        });

        Boolean success = transactionTemplate.execute(status -> {
            if (!toDelete.isEmpty()) {
                return articleTagRepository.deleteArticleTags(toDelete);
            }
            if (!newTags.isEmpty()) {
                return articleTagRepository.saveArticleTags(articleId, newTags);
            }
            return true;
        });
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void saveTags(Integer articleId, List<Integer> tagIds) {
        boolean success = articleTagRepository.saveArticleTags(articleId, tagIds);
    }

}
