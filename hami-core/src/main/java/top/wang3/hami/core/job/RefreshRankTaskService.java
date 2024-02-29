package top.wang3.hami.core.job;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.message.email.AlarmEmailMessage;
import top.wang3.hami.core.init.RankListInitializer;
import top.wang3.hami.core.service.mail.MailMessageHandler;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings(value = "all")
public class RefreshRankTaskService {

    private final RankListInitializer initializer;
    private final MailMessageHandler handler;

    @Scheduled(cron = "11 11 1 * * ?")
    public void refreshHotArticles() {
        try {
            long start = System.currentTimeMillis();
            log.info("start to refresh cate-article rank list");
            initializer.refreshCateHotArticle();
            long end = System.currentTimeMillis();
            log.info("refresh cate-article rank list success, cost: {}ms", end -start);
        } catch (Exception e) {
            e.printStackTrace();
            handler.handle(new AlarmEmailMessage("ArticleRank定时任务告警信息", e.getMessage()));
        }
    }

    @Scheduled(cron = "22 22 1 * * ?")
    public void refreshOverallHotArticles() {
        try {
            long start = System.currentTimeMillis();
            log.info("start to refresh overall-article rank list");
            initializer.refreshOverallHotArticle();
            long end = System.currentTimeMillis();
            log.info("refresh overall-article list success, cost: {}ms", end - start);
        } catch (Exception e) {
            e.printStackTrace();
            handler.handle(new AlarmEmailMessage("ArticleRank定时任务告警信息", e.getMessage()));
        }
    }

    @Scheduled(cron = "33 33 1 * * ?")
    public void refreshAuthorRankList() {
        try {
            long start = System.currentTimeMillis();
            log.info("start to refresh author-rank list");
            initializer.refreshAuthorRankList();
            long end = System.currentTimeMillis();
            log.info("refresh author-rank list success, cost: {}ms", end - start);
        } catch (Exception e) {
            e.printStackTrace();
            handler.handle(new AlarmEmailMessage("AuthorRankList定时任务告警信息", e.getMessage()));
        }
    }
}
