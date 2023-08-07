package top.wang3.hami.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.wang3.hami.security.service.JwtTokenService;
import top.wang3.hami.security.service.TokenService;
import top.wang3.hami.security.storage.BlacklistStorage;
import top.wang3.hami.security.storage.DefaultBlackListStorage;

@Configuration
public class TokenServiceConfig {

    @Bean
    @ConditionalOnMissingBean
    public TokenService tokenService(@Autowired WebSecurityProperties properties, @Autowired BlacklistStorage storage) {
        return new JwtTokenService(properties, storage);
    }


    @Bean
    @ConditionalOnMissingBean
    public BlacklistStorage blacklistStorage() {
        return new DefaultBlackListStorage();
    }

}
