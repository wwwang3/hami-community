package top.wang3.hami.core.init;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.mapper.ArticleMapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Order(3)
@RequiredArgsConstructor
public class ArticleCacheInitializer implements HamiInitializer {


    private final ArticleMapper articleMapper;

    @Override
    public InitializerEnums getName() {
        return InitializerEnums.ARTICLE_CACHE;
    }

    @Override
    public void run() {
        cacheArticle();
    }

    private void cacheArticle() {
        ListMapperHandler.scanDesc(
                Integer.MAX_VALUE,
                500,
                1000,
                articleMapper::scanArticleDesc,
                articles -> {
                    // 文章内容
                    cacheContent(articles);
                    // 文章信息
                    cacheArticleInfo(articles);
                },
                Article::getId
        );
    }

    private void cacheArticleInfo(List<Article> articles) {
        Map<String, Article> map = ListMapperHandler.listToMap(
                articles,
                item -> RedisConstants.ARTICLE_INFO + item.getId(),
                item -> {
                    item.setContent(null);
                    return item;
                });
        RedisClient.cacheMultiObject(map, TimeoutConstants.ARTICLE_INFO_EXPIRE, TimeUnit.MILLISECONDS);
    }

    private void cacheContent(List<Article> articles) {
        Map<String, String> map = ListMapperHandler.listToMap(articles,
                item -> RedisConstants.ARTICLE_CONTENT + item.getId(), Article::getContent);
        RedisClient.cacheMultiObject(map, TimeoutConstants.ARTICLE_INFO_EXPIRE, TimeUnit.MILLISECONDS);
    }
}
