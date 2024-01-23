package top.wang3.hami.core.init;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.model.Category;
import top.wang3.hami.common.model.HotCounter;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.article.CategoryService;
import top.wang3.hami.core.service.stat.repository.ArticleStatRepository;
import top.wang3.hami.core.service.stat.repository.UserStatRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Component
@Order(1)
@Slf4j
@RequiredArgsConstructor
public class RankListInitializer implements HamiInitializer {

    private final CategoryService categoryService;
    private final ArticleStatRepository articleStatRepository;
    private final UserStatRepository userStatRepository;

    @Override
    public InitializerEnums getName() {
        return InitializerEnums.RANK_LIST;
    }

    @Override
    public boolean alwaysExecute() {
        return true;
    }

    @Override
    public boolean async() {
        return true;
    }

    @Override
    public void run() {
        refreshCateHotArticle();
        refreshOverallHotArticle();
        refreshAuthorRankList();
    }

    public void refreshCateHotArticle() {
        List<Category> categories =
                categoryService.getAllCategories();
        categories.forEach(category -> {
            String redisKey = RedisConstants.CATE_HOT_ARTICLE + category.getId();
            // 3个月内的
            LocalDateTime dateTime = LocalDateTime.now().minusMonths(3);
            List<HotCounter> articles = articleStatRepository.loadHotArticle(category.getId(), dateTime);
            if (articles != null && !articles.isEmpty()) {
                refresh(redisKey, articles);
            }
        });
    }

    public void refreshOverallHotArticle() {
        String redisKey = RedisConstants.OVERALL_HOT_ARTICLE;
        // 6个月内
        LocalDateTime dateTime = LocalDateTime.now().minusMonths(6);
        List<HotCounter> articles = articleStatRepository.loadHotArticle(null, dateTime);
        refresh(redisKey, articles);
    }

    public void refreshAuthorRankList() {
        List<HotCounter> counters = userStatRepository.loadAuthorRankList();
        refresh(RedisConstants.AUTHOR_RANKING, counters);
    }

    private void refresh(String key, List<HotCounter> counters) {
        Collection<Tuple> tuples = ListMapperHandler.listToTuple(
                counters,
                HotCounter::getItemId,
                HotCounter::getHotIndex
        );
        RedisClient.zSetAll(key, tuples);
    }
}
