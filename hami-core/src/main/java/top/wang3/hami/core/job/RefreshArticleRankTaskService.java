package top.wang3.hami.core.job;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.Category;
import top.wang3.hami.common.model.HotCounter;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.article.ArticleStatService;
import top.wang3.hami.core.service.article.CategoryService;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings(value = {"unchecked"})
public class RefreshArticleRankTaskService {

    private final CategoryService categoryService;
    private final ArticleStatService articleStatService;


    @Scheduled(cron = "* */10 * * * *")
    @Async
    public void refreshHotArticles() {
        try {
            log.info("start to refresh cate-article rank list");
            List<Category> categories =
                    categoryService.getAllCategories();
            categories.forEach(category -> {
                String redisKey = Constants.HOT_ARTICLE + category.getId();
                List<HotCounter> articles = articleStatService.getHotArticlesByCateId(category.getId());
                if (articles != null && !articles.isEmpty()){
                    RedisClient.zAddAll(redisKey, convertToTuple(articles));
                }
            });
            log.info("refresh cate-article rank list success");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("error_class: {}, error_msg: {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    @Scheduled(cron = "* */15 * * * *")
    @Async
    public void refreshOverallHotArticles() {
        try {
            log.info("start to refresh overall-article rank list");
            String redisKey = Constants.OVERALL_HOT_ARTICLES;
            List<HotCounter> articles = articleStatService.getOverallHotArticles();
            RedisClient.zAddAll(redisKey, convertToTuple(articles));
            log.info("refresh overall-article list success");
        } catch (Exception e) {
            log.error("error_class: {}, error_msg: {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private Set<ZSetOperations.TypedTuple<Integer>> convertToTuple(List<HotCounter> counters) {
        return ListMapperHandler.listToSet(counters, item -> {
           return new DefaultTypedTuple<>(item.getArticleId(), item.getHotRank());
        });
    }
}
