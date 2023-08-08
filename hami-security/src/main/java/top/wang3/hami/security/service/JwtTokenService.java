package top.wang3.hami.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import top.wang3.hami.security.config.WebSecurityProperties;
import top.wang3.hami.security.model.LoginUser;
import top.wang3.hami.security.storage.BlacklistStorage;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

/**
 * TokenService实现类 token类型为JWT
 * todo 完善
 */
@Slf4j
public class JwtTokenService implements TokenService {

    private final WebSecurityProperties properties;

    private final BlacklistStorage storage;

    private final SignatureAlgorithm algorithm = SignatureAlgorithm.HS256;
    private final Key key;

    public JwtTokenService(WebSecurityProperties properties, BlacklistStorage blacklistStorage) {
        this.properties = properties;
        this.storage = blacklistStorage;
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
        //todo 完善
        return Jwts.builder()
                .signWith(key) //签名
                .setExpiration(expiration) //过期时间
                .setId(UUID.randomUUID().toString()) //jwtId
                .claim("id", id) //claims
                .compact();
    }

    @Override
    public LoginUser resolveToken(String jwt) {
        Claims claims = parseJwt(jwt);
        if (claims == null) return null;
        String jwtId = claims.getId();
        //在黑名单中
        if (storage.contains(jwtId)) {
            return null;
        }
        return LoginUser
                .withId(claims.get("id", int.class))
                .build();
    }

    @Override
    public boolean invalidate(String jwt) {
        //将jwt加入到黑名单
        //解析时会判断是否在黑名单中
        Claims claims = parseJwt(jwt);
        //解析失败了 说明token已经无效了
        if (claims == null) return true;
        return storage.add(claims.getId(), claims.getExpiration().getTime());
    }


    private Claims parseJwt(String jwt) {
        try {
            return (Claims) Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parse(jwt)
                    .getBody();
        } catch (JwtException e) {
            log.debug("parse jwt failed, error_class: {}, error_msg: {}", e.getClass().getName(),
                    e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) throws ClassNotFoundException {
        String key = "awdkawjkgsadawddawdawfgsegregergerg";
        Key signedKey = new SecretKeySpec(key.getBytes(), SignatureAlgorithm.HS256.getJcaName());
        for (int i = 0; i < 100; i++) {
            long start = System.currentTimeMillis();
            String jwt = Jwts.builder()
                    .setId(UUID.randomUUID().toString())
                    .signWith(signedKey)
                    .claim("id", 123)
                    .setExpiration(new Date(System.currentTimeMillis() + 100000))
                    .compact();
            long end = System.currentTimeMillis();
            System.out.println(end - start);
        }
    }

}
