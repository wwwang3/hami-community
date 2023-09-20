package top.wang3.hami.core.job;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.converter.ArticleConverter;
import top.wang3.hami.common.dto.article.ArticleStatDTO;
import top.wang3.hami.common.dto.user.UserStat;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.repository.ArticleStatRepository;
import top.wang3.hami.core.repository.UserRepository;
import top.wang3.hami.core.service.stat.CountService;
import top.wang3.hami.core.service.stat.impl.SimpleCountService;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshStatTaskService {

    private final ArticleStatRepository articleStatRepository;
    private final UserRepository userRepository;

    private final SimpleCountService simpleCountService;

    /**
     * 全量刷新文章数据缓存
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Async
    public void refreshArticleStat() {
        log.info("start to refresh article-data");
        long start = System.currentTimeMillis();
        //全量刷新文章数据
        //数据多了肯定g了
        int batchSize = 500;
        int lastArticleId = 0;
        while (true) {
            List<ArticleStat> stats = articleStatRepository.scanArticleStats(lastArticleId, batchSize);
            cacheStat(stats);
            if (stats == null || stats.size() < batchSize) {
                break;
            }
            lastArticleId = stats.get(stats.size() - 1).getArticleId();
        }
        long end = System.currentTimeMillis();
        log.info("refresh article-stat success, cost: {}ms", end - start);
    }

    @Scheduled(cron = "0 30 2 * * ?")
    public void refreshUserStat() {
        log.info("start to refresh user-data");
        long start = System.currentTimeMillis();
        int batchSize = 500;
        int lastUserId = 0;
        while (true) {
            List<Integer> userIds = userRepository.scanUserIds(lastUserId, batchSize);
            if (userIds.size() < batchSize) {
                break;
            }
            List<UserStat> stats = simpleCountService.getUserStatByUserIds(userIds);
            cacheUserStat(stats);
            lastUserId = userIds.get(userIds.size() - 1);
        }
        long end = System.currentTimeMillis();
        log.info("refresh user-stat success, cost: {}ms", end - start);
    }


    private void cacheStat(List<ArticleStat> stats) {
        if (stats == null || stats.isEmpty()) return;
        stats.forEach(stat -> {
            ArticleStatDTO dto = ArticleConverter.INSTANCE.toArticleStatDTO(stat);
            String redisKey = Constants.COUNT_TYPE_ARTICLE + stat.getArticleId();
            RedisClient.setCacheObject(redisKey, dto);
        });
    }

    private void cacheUserStat(List<UserStat> stats) {
        if (stats == null || stats.isEmpty()) {
            return;
        }

        stats.forEach(stat -> {
            Map<String, Integer> map = CountService.setUserStatToMap(stat);
            RedisClient.hMSet(Constants.COUNT_TYPE_USER + stat.getUserId(), map);
        });
    }


}
