package top.wang3.hami.security.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * UserDetails实现类，兼容username-password登录
 */
@Data
@AllArgsConstructor
public class LoginUser implements UserDetails, CredentialsContainer {

    /**
     * 用户ID
     */
    private final int id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码
     */
    @JsonIgnore
    private String password;

    /**
     * 权限集合
     */
    private List<GrantedAuthority> authorities;

    /**
     * 过期时间
     * 创建token时设置
     * @see top.wang3.hami.security.service.TokenService#createToken(LoginUser)
     */
    private Date expireAt;

    @Override
    public List<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return username;
    }


    @JsonIgnore
    @Override
    public String getPassword() {
        return password;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }

    public static LoginUserBuilder withId(int id) {
        return builder().id(id);
    }

    public static LoginUserBuilder builder() {
        return new LoginUserBuilder();
    }

    public static class LoginUserBuilder {
        int id;
        Date expireAt;
        String username;

        String email;
        String password;

        List<GrantedAuthority> authorities;

        public LoginUserBuilder id(int id) {
            this.id = id;
            return this;
        }

        public LoginUserBuilder expireAt(Date expireAt) {
            this.expireAt = expireAt;
            return this;
        }

        public LoginUserBuilder username(String username) {
            this.username = username;
            return this;
        }

        public LoginUserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public LoginUserBuilder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * 添加角色信息
         * @param roles 角色
         * @return LoginUserBuilder
         */
        public LoginUserBuilder roles(String... roles) {
            if (this.authorities == null) {
                this.authorities = new ArrayList<>();
            }
            this.authorities.addAll(
                    Arrays.stream(roles)
                            .map(s -> new SimpleGrantedAuthority(s.startsWith("ROLE_") ? s : "ROLE_" + s))
                            .toList()
            );
            return this;
        }

        /**
         * 添加权限信息
         * @param permissions 权限
         * @return LoginUserBuilder
         */
        public LoginUserBuilder permissions(String... permissions) {
            if (authorities == null) {
                authorities = new ArrayList<>();
            }
            this.authorities.addAll(
                    Arrays.stream(permissions)
                            .map(SimpleGrantedAuthority::new)
                            .toList()
            );
            return this;
        }

        /**
         * 直接设置角色权限信息
         * @param authorities 权限
         * @return LoginUserBuilder
         */
        public LoginUserBuilder authorities(List<GrantedAuthority> authorities) {
            if (authorities != null) {
                this.authorities = authorities;
            }
            return this;
        }

        public LoginUser build() {
            return new LoginUser(id, username, email, password, authorities, expireAt);
        }
    }
}
