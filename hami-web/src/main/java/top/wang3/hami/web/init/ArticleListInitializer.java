package top.wang3.hami.web.init;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.model.Category;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.article.repository.ArticleRepository;
import top.wang3.hami.core.service.article.repository.CategoryRepository;

import java.util.List;

//@Component
@Slf4j
@Order(3)
public class ArticleListInitializer implements ApplicationRunner {

    @Resource
    ArticleRepository articleRepository;

    @Resource
    CategoryRepository categoryRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            log.info("start to init cate-article-list");
            long start = System.currentTimeMillis();
            cacheTotal();
            cacheSub();
            long end = System.currentTimeMillis();
            log.info("finish init cate-article-list, cost: {}ms", end - start);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cacheTotal() {
        List<Article> articles = articleRepository.listArticleByCateId(null);
        String totalKey = Constants.ARTICLE_LIST;
        cacheToRedis(totalKey, articles);
    }

    private void cacheSub() {
        List<Category> categories = categoryRepository.getAllCategories();
        for (Category category : categories) {
            String key = Constants.CATE_ARTICLE_LIST + category.getId();
            List<Article> data = articleRepository.listArticleByCateId(category.getId());
            cacheToRedis(key, data);
        }
    }

    private void cacheToRedis(final String key, List<Article> articles) {
        if (CollectionUtils.isEmpty(articles)) {
            return;
        }
        RedisClient.deleteObject(key);
        List<List<Article>> results = ListMapperHandler.split(articles, 2000);
        results.forEach(members -> {
            var set = ListMapperHandler.listToZSet(members, Article::getId,
                    article -> (double) article.getCtime().getTime());
            RedisClient.zAddAll(key, set);
        });
    }
}
