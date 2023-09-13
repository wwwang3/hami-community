package top.wang3.hami.core.service.stat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import top.wang3.hami.common.canal.CanalEntryHandler;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.core.service.stat.handler.ArticleStatCanalHandler;
import top.wang3.hami.core.service.stat.handler.UserStatCanalHandler;
import top.wang3.hami.core.service.stat.impl.CachedCountService;
import top.wang3.hami.core.service.stat.impl.SimpleCountService;

@Configuration
@ConditionalOnProperty(prefix = "hami.count-cache", name = "enable", havingValue = "true", matchIfMissing = true)
@Slf4j
public class CachedCountServiceConfig {

    @Bean
    @Order(1)
    public CanalEntryHandler<ArticleStat> articleStatCanalEntryHandler() {
        return new ArticleStatCanalHandler();
    }

    @Bean
    @Order(2)
    public CanalEntryHandler<ArticleStat> userStatCanalEntryHandler() {
        return new UserStatCanalHandler();
    }

    @Bean
    @Primary
    public CountService cachedCountService(@Qualifier("simpleCountService") SimpleCountService countService) {
        log.debug("cached count service");
        return new CachedCountService(countService);
    }
}
