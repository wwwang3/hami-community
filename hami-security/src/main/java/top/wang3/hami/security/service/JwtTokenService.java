package top.wang3.hami.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.security.context.LoginUserContext;
import top.wang3.hami.security.model.LoginUser;
import top.wang3.hami.security.model.WebSecurityProperties;
import top.wang3.hami.security.storage.BlacklistStorage;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * TokenService实现类 token类型为JWT
 */
@Slf4j
public class JwtTokenService implements TokenService {

    private final WebSecurityProperties properties;

    private final BlacklistStorage storage;

    private final Key key;

    public JwtTokenService(WebSecurityProperties properties, BlacklistStorage blacklistStorage) {
        this.properties = properties;
        this.storage = blacklistStorage;
        SignatureAlgorithm algorithm = SignatureAlgorithm.HS256;
        key = new SecretKeySpec(properties.getSecret().getBytes(), algorithm.getJcaName());
    }

    @Override
    public String createToken(LoginUser loginUser) {
        //登录用户的ID
        int id = loginUser.getId();
        //有效期 单位s
        long timeout = properties.getExpire();
        Date expiration = new Date(timeout * 1000 + System.currentTimeMillis());
        //set expireAt, used when setting cookie
        loginUser.setExpireAt(expiration);
        return Jwts.builder()
                .signWith(key) //签名
                .setExpiration(expiration) //过期时间
                .setId(UUID.randomUUID().toString()) //jwtId
                .claim("id", id) //claims
                .claim("authorities", ListMapperHandler.listTo(loginUser.getAuthorities(), GrantedAuthority::getAuthority))
                .compact();
    }

    @SuppressWarnings(value = {"unchecked"})
    @Override
    public LoginUser resolveToken(String jwt) {
        //null判断交给parseJwt
        Claims claims = parseJwt(jwt);
        //解析失败 或者在 黑名单中
        if (invalided(claims)) {
            return null;
        }
        //unchecked
        List<String> authorities = claims.get("authorities", List.class);
        return LoginUser
                .withId(claims.get("id", Integer.class))
                .username(claims.get("username", String.class))
                .email(claims.get("email", String.class))
                .authorities(ListMapperHandler.listTo(authorities, SimpleGrantedAuthority::new))
                .expireAt(claims.getExpiration())
                .build();
    }

    @Override
    public boolean invalidate(String jwt) {
        //将jwt加入到黑名单
        Claims claims = parseJwt(jwt);
        //token解析失败了 说明没有携带token或者token失效 返回invalid token信息
        //已经于黑名单中或者claims为空, 应该提示token无效, 而不是退出成功
        if (invalided(claims)) {
            return false;
        }
        return storage.add(claims.getId(), claims.getExpiration().getTime());
    }


    @Override
    public void kickout() {
        //jwt黑名单机制，无法做到清除全部登录态，除非使用白名单机制，在服务器保存token，这样又回到token-session模型了
        //违背了jwt的初衷，黑名单机制其实违背了
        HttpServletRequest request = LoginUserContext.getRequest();
        String token = getToken(request, properties.getTokenName());
        invalidate(token);
    }


    private Claims parseJwt(String jwt) {
        //解析token, jwt不合法或者解析失败返回bull
        try {
            return (Claims) Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parse(jwt)
                    .getBody();
        } catch (Exception e) {
//            log.debug("parse jwt failed, error_class: {}, error_msg: {}", e.getClass().getSimpleName(),
//                    e.getMessage());
            return null;
        }
    }

    private boolean invalided(Claims claims) {
        return claims == null || storage.contains(claims.getId());
    }
}
