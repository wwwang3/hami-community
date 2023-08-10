package top.wang3.hami.security.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
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
    public TokenService jwtTokenService(@Autowired WebSecurityProperties properties,  @Autowired BlacklistStorage storage) {
        log.debug("storage impl: {}", storage.getClass().getSimpleName());
        return new JwtTokenService(properties, storage);
    }

    @Bean("defaultBlacklistStorage")
    @ConditionalOnMissingBean
    public BlacklistStorage defaultBlacklistStorage() {
        return new DefaultBlackListStorage();
    }

    @Bean("redisBlackListStorage")
    @ConditionalOnBean(RedisTemplate.class)
    public BlacklistStorage redisBlackListStorage(RedisTemplate<String, Object> redisTemplate) {
        return new RedisBlackListStorage(redisTemplate);
    }

}
