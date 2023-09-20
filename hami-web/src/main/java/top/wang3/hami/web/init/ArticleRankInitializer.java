package top.wang3.hami.web.init;

import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import top.wang3.hami.core.job.RefreshArticleRankTaskService;

@Component
public class ArticleRankInitializer implements ApplicationRunner {

    @Resource
    RefreshArticleRankTaskService service;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        service.refreshHotArticles();
        service.refreshOverallHotArticles();
    }
}
