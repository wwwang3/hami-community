package top.wang3.hami.security.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import top.wang3.hami.security.model.WebSecurityProperties;
import top.wang3.hami.security.service.JwtTokenService;
import top.wang3.hami.security.service.TokenService;
import top.wang3.hami.security.storage.BlacklistStorage;
import top.wang3.hami.security.storage.DefaultBlackListStorage;
import top.wang3.hami.security.storage.RedisBlackListStorage;

@Configuration
@Slf4j
public class JwtTokenServiceConfig {


    @Bean("jwtTokenService")
    @ConditionalOnMissingBean
    public TokenService jwtTokenService(WebSecurityProperties properties, BlacklistStorage storage) {
        log.debug("storage impl: {}", storage.getClass().getSimpleName());
        return new JwtTokenService(properties, storage);
    }

    @Configuration
    @ConditionalOnClass(StringRedisTemplate.class)
    public static class RedisBlackListConfig {

        @Bean("redisBlackListStorage")
        @ConditionalOnMissingBean
        public BlacklistStorage redisBlackListStorage(StringRedisTemplate redisTemplate) {
            return new RedisBlackListStorage(redisTemplate);
        }
    }


    @Bean("defaultBlacklistStorage")
    @ConditionalOnMissingBean
    public BlacklistStorage defaultBlacklistStorage() {
        return new DefaultBlackListStorage();
    }



}
