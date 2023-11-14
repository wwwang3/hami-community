package top.wang3.hami.core.job;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import top.wang3.hami.core.init.HotArticleListInitializer;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings(value = "all")
public class RefreshArticleRankTaskService {

    private final HotArticleListInitializer initializer;

    @Async
    @Scheduled(cron = "33 33 2 * * ? ")
    public void refreshHotArticles() {
        try {
            long start = System.currentTimeMillis();
            log.info("start to refresh cate-article rank list");
            initializer.refreshHotArticles();
            long end = System.currentTimeMillis();
            log.info("refresh cate-article rank list success, cost: {}ms", end -start);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("error_class: {}, error_msg: {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    @Async
    @Scheduled(cron = "33 36 2 * * ? ")
    public void refreshOverallHotArticles() {
        try {
            long start = System.currentTimeMillis();
            log.info("start to refresh overall-article rank list");
            initializer.refreshOverallHotArticles();
            long end = System.currentTimeMillis();
            log.info("refresh overall-article list success, cost: {}ms", end - start);
        } catch (Exception e) {
            log.error("error_class: {}, error_msg: {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }
}
