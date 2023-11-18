package top.wang3.hami.core.job;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.message.email.AlarmEmailMessage;
import top.wang3.hami.core.init.StatInitializer;
import top.wang3.hami.core.service.mail.MailMessageHandler;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshStatTaskService {

    private final StatInitializer statInitializer;
    private final MailMessageHandler handler;

    /**
     * 全量刷新文章数据缓存
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Async
    public void refreshArticleStat() {
        try {
            log.info("start to refresh article-data");
            long start = System.currentTimeMillis();
            //全量刷新文章数据
            //数据多了肯定g了
            statInitializer.cacheArticleStat();
            long end = System.currentTimeMillis();
            log.info("refresh article-stat success, cost: {}ms", end - start);
        } catch (Exception e) {
            handler.handle(new AlarmEmailMessage("RefreshStat定时任务告警信息", e.getMessage()));
            log.error("error_class: {}, error_msg: {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    @Scheduled(cron = "0 20 2 * * ?")
    @Async
    public void refreshUserStat() {
        try {
            log.info("start to refresh user-data");
            long start = System.currentTimeMillis();
            statInitializer.cacheUserStat();
            long end = System.currentTimeMillis();
            log.info("refresh user-stat success, cost: {}ms", end - start);
        } catch (Exception e) {
            handler.handle(new AlarmEmailMessage("RefreshStat定时任务告警信息", e.getMessage()));
            log.error("error_class: {}, error_msg: {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }

}
