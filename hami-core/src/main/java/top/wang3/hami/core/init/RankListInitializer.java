package top.wang3.hami.core.init;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.model.Category;
import top.wang3.hami.common.model.HotCounter;
import top.wang3.hami.common.util.DateUtils;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.article.CategoryService;
import top.wang3.hami.core.service.stat.repository.ArticleStatRepository;
import top.wang3.hami.core.service.stat.repository.UserStatRepository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Component
@Order(1)
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("")
public class RankListInitializer implements HamiInitializer {

    private final CategoryService categoryService;
    private final ArticleStatRepository articleStatRepository;
    private final UserStatRepository userStatRepository;

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
        refreshAuthorRankList();
    }

    public void refreshHotArticles() {
        List<Category> categories =
                categoryService.getAllCategories();
        categories.forEach(category -> {
            String redisKey = RedisConstants.HOT_ARTICLE + category.getId();
            long time = DateUtils.offsetMonths(new Date(), -3);
            List<HotCounter> articles = articleStatRepository.getHotArticlesByCateId(category.getId(), time);
            if (articles != null && !articles.isEmpty()) {
                refresh(redisKey, articles);
            }
        });
    }

    public void refreshOverallHotArticles() {
        String redisKey = RedisConstants.OVERALL_HOT_ARTICLES;
        long time = DateUtils.offsetMonths(new Date(), -3);
        List<HotCounter> articles = articleStatRepository.getOverallHotArticles(time);
        refresh(redisKey, articles);
    }

    public void refreshAuthorRankList() {
        List<HotCounter> counters = userStatRepository.getAuthorRankList();
        refresh(RedisConstants.AUTHOR_RANKING, counters);
    }

    private void refresh(String key, List<HotCounter> counters) {
        Collection<Tuple> tuples = ListMapperHandler.listToTuple(counters, HotCounter::getItemId, HotCounter::getHotIndex);
        RedisClient.zSetAll(key, tuples);
    }
}
