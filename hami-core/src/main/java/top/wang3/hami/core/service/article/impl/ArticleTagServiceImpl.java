package top.wang3.hami.core.service.article.impl;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.ArticleTag;
import top.wang3.hami.core.repository.ArticleTagRepository;
import top.wang3.hami.core.service.article.ArticleTagService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleTagServiceImpl implements ArticleTagService {

    @Resource
    TransactionTemplate transactionTemplate;

    private final ArticleTagRepository articleTagRepository;


    @Cacheable(cacheNames = Constants.REDIS_CACHE_NAME, key = "'#article:tag:'+#articleId",
        cacheManager = Constants.RedisCacheManager)
    @Override
    public List<Integer> getArticleTagIds(Integer articleId) {
        return articleTagRepository.getArticleTagIdsById(articleId);
    }

    @CacheEvict(cacheNames = Constants.REDIS_CACHE_NAME, key = "'#article:tag:'+#articleId",
            cacheManager = Constants.RedisCacheManager)
    @Override
    public void updateTags(Integer articleId, List<Integer> newTags) {
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

        transactionTemplate.execute(status -> {
            if (!toDelete.isEmpty()) {
                articleTagRepository.deleteArticleTags(toDelete);
            }
            if (!newTags.isEmpty()) {
                articleTagRepository.saveArticleTags(articleId, newTags);
            }
            return null;
        });

    }

    @Override
    public void saveTags(Integer articleId, List<Integer> tagIds) {
        boolean success = articleTagRepository.saveArticleTags(articleId, tagIds);
    }

//    private void deleteCache(String key) {
//        RedisClient.deleteObject(key);
//    }

}
