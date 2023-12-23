package top.wang3.hami.core.init;


import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.model.Category;
import top.wang3.hami.common.model.HotCounter;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.article.CategoryService;
import top.wang3.hami.core.service.stat.repository.ArticleStatRepository;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
@Order(1)
@Slf4j
@RequiredArgsConstructor
public class HotArticleListInitializer implements HamiInitializer {

    private final CategoryService categoryService;
    private final ArticleStatRepository articleStatRepository;

    @Override
    public InitializerEnums getName() {
        return InitializerEnums.HOT_ARTICLE;
    }

    @Override
    public boolean alwaysExecute() {
        return true;
    }

    @Override
    public void run() {
        refreshHotArticles();
        refreshOverallHotArticles();
    }

    public void refreshHotArticles() {
        List<Category> categories =
                categoryService.getAllCategories();
        categories.forEach(category -> {
            String redisKey = RedisConstants.HOT_ARTICLE + category.getId();
            long time = DateUtil.offsetMonth(new Date(), -3).getTime();
            List<HotCounter> articles = articleStatRepository.getHotArticlesByCateId(category.getId(), time);
            RedisClient.deleteObject(redisKey);
            if (articles != null && !articles.isEmpty()) {
                RedisClient.zAddAll(redisKey, convertToTuple(articles));
            }
        });
    }

    public void refreshOverallHotArticles() {
        String redisKey = RedisConstants.OVERALL_HOT_ARTICLES;
        RedisClient.deleteObject(redisKey);
        long time = DateUtil.offsetMonth(new Date(), -3).getTime();
        List<HotCounter> articles = articleStatRepository.getOverallHotArticles(time);
        RedisClient.zAddAll(redisKey, convertToTuple(articles));
    }

    private Set<ZSetOperations.TypedTuple<Integer>> convertToTuple(List<HotCounter> counters) {
        return ListMapperHandler.listToSet(counters, item -> {
            return new DefaultTypedTuple<>(item.getArticleId(), item.getHotRank());
        });
    }
}
