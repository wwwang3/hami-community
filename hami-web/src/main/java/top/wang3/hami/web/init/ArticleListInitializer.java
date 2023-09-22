package top.wang3.hami.web.init;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.model.Category;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.repository.ArticleRepository;
import top.wang3.hami.core.repository.CategoryRepository;

import java.util.List;

@Component
@Slf4j
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
            List<Article> articles = articleRepository.listArticleByCateId(null);
            String totalKey = Constants.ARTICLE_LIST;
            cacheToRedis(totalKey, articles);
            List<Category> categories = categoryRepository.getAllCategories();
            for (Category category : categories) {
                String key = Constants.CATE_ARTICLE_LIST + category.getId();
                List<Article> data = articleRepository.listArticleByCateId(category.getId());
                cacheToRedis(key, data);
            }
            long end = System.currentTimeMillis();
            log.info("finish init cate-article-list, cost: {}ms", end - start);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cacheToRedis(String key, List<Article> articles) {
        var set = ListMapperHandler.listToZSet(articles, Article::getId, article -> {
            return (double) article.getCtime().getTime();
        });
        RedisClient.zAddAll(key, set); //no-expire
    }
}
