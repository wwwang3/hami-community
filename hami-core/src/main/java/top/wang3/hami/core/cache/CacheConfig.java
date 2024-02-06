package top.wang3.hami.core.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import top.wang3.hami.common.constant.Constants;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
@Slf4j
public class CacheConfig {

    @Bean
    public Caffeine<Object, Object> caffeine() {
        return Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.DAYS) // 缓存标签和分类数据
                .initialCapacity(128)
                .maximumSize(512)
                .removalListener((k, v, reason) -> log.warn("##cache k: {} v: {} removed cause: {}", k, v, reason.name()));
    }

    @Bean(Constants.CaffeineCacheManager)
    @Primary
    public CacheManager caffeineCacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager manager = new CaffeineCacheManager(Constants.CAFFEINE_CACHE_NAME);
        manager.setCaffeine(caffeine);
        manager.setAllowNullValues(true);
        return manager;
    }
}
