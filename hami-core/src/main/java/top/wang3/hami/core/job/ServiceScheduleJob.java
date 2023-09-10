package top.wang3.hami.core.job;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.article.ArticleStatService;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceScheduleJob {

    private final ArticleStatService articleStatService;

    /**
     * 全量刷新文章数据缓存
     */
    @Scheduled(cron = "0 33 2 * * ?")
    @Async
    public void refreshArticleStat() {
        //全量刷新文章数据
        //数据多了肯定g了
        int batchSize = 100;
        int lastArticleId = 0;
        while (true) {
            List<ArticleStat> stats = articleStatService.scanArticleStats(lastArticleId, batchSize);
            cacheStat(stats);
            if (stats.size() < batchSize) {
                break;
            }
            lastArticleId = stats.get(stats.size() - 1).getArticleId();
        }
    }


    private void cacheStat(List<ArticleStat> stats) {
        stats.forEach(stat -> {
            Map<String, Integer> map = Map.of(Constants.ARTICLE_LIKES, stat.getLikes(), Constants.ARTICLE_COMMENTS, stat.getComments(),
                    Constants.ARTICLE_COLLECTS, stat.getCollects(),
                    Constants.ARTICLE_VIEWS, stat.getViews()
            );
            RedisClient.hMSet(Constants.COUNT_TYPE_ARTICLE + stat.getArticleId(),map);
        });
    }

}
