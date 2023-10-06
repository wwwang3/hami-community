package top.wang3.hami.core.job;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.core.service.article.repository.ArticleRepository;
import top.wang3.hami.core.service.article.repository.ArticleStatRepository;
import top.wang3.hami.core.service.stat.CountService;
import top.wang3.hami.core.service.user.repository.UserRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshStatTaskService {

    private final ArticleRepository articleRepository;

    private final ArticleStatRepository articleStatRepository;

    private final UserRepository userRepository;

    private final CountService countService;

    /**
     * 全量刷新文章数据缓存
     */
    @Scheduled(cron = "0 0 2 * * ?")
//    @Async
    public void refreshArticleStat() {
        log.info("start to refresh article-data");
        long start = System.currentTimeMillis();
        //全量刷新文章数据
        //数据多了肯定g了
        int batchSize = 500;
        int lastArticleId = 0;
        while (true) {
            List<Integer> ids = articleRepository.scanArticleIds(lastArticleId, batchSize);
            if (!CollectionUtils.isEmpty(ids)) {
                countService.getArticleStatByIds(ids);
                lastArticleId = ids.get(ids.size() - 1);
            } else {
                break;
            }
        }
        long end = System.currentTimeMillis();
        log.info("refresh article-stat success, cost: {}ms", end - start);
    }

    @Scheduled(cron = "0 40 2 * * ?")
    public void refreshUserStat() {
        log.info("start to refresh user-data");
        long start = System.currentTimeMillis();
        int batchSize = 500;
        int lastUserId = 0;
        while (true) {
            List<Integer> userIds = userRepository.scanUserIds(lastUserId, batchSize);
            if (!CollectionUtils.isEmpty(userIds)) {
                countService.getUserStatByUserIds(userIds);
                lastUserId = userIds.get(userIds.size() - 1);
            } else {
                break;
            }
        }
        long end = System.currentTimeMillis();
        log.info("refresh user-stat success, cost: {}ms", end - start);
    }

}
