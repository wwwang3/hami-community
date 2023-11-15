package top.wang3.hami.core.init;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.article.repository.ArticleRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Order(3)
@RequiredArgsConstructor
public class ArticleCacheInitializer implements HamiInitializer {


    private final ArticleRepository articleRepository;
    private final ArticleService articleService;

    @Override
    public InitializerEnums getName() {
        return InitializerEnums.ARTICLE_CACHE;
    }

    @Override
    public void run() {
        cacheArticle();
        cacheArticleContent();
    }

    private void cacheArticle() {
        int lastId = 0;
        while (true) {
            List<Integer> ids = articleRepository.scanArticleIds(lastId, 1000);
            if (ids == null || ids.isEmpty()) {
                break;
            }
            articleService.loadArticleInfoCache(ids);
            lastId = ids.get(ids.size() - 1);
        }
    }

    private void cacheArticleContent() {
        int lastId = 0;
        while (true) {
            List<Article> articles = articleRepository.scanArticleContent(lastId, 500);
            if (articles == null || articles.isEmpty()) {
                break;
            }
            cacheContent(articles);
            lastId = articles.get(articles.size() - 1).getId();
        }
    }

    private void cacheContent(List<Article> articles) {
        Map<String, String> map = ListMapperHandler.listToMap(articles,
                item -> RedisConstants.ARTICLE_CONTENT + item.getId(), Article::getContent);
        RedisClient.cacheMultiObject(map, 1, 30, TimeUnit.DAYS);
    }
}
