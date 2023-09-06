package top.wang3.hami.core.job;


import lombok.RequiredArgsConstructor;
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
            Map<String, Integer> map = Map.of(Constants.COUNT_LIKES, stat.getLikes(), Constants.COUNT_COMMENTS, stat.getComments(),
                    Constants.COUNT_COLLECTS, stat.getCollects(),
                    Constants.COUNT_VIEWS, stat.getViews()
            );
            RedisClient.hMSet(Constants.COUNT_ARTICLE_STAT + stat.getArticleId(),map);
        });
    }

}
