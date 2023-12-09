package top.wang3.hami.security.ratelimit.annotation;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyMeta {

    /**
     * 方法名 过滤器中使用时为空
     */
    private String methodName;

    /**
     * 类名 过滤器中使用时为空
     */
    private String className;

    /**
     * 请求URI
     */
    private String uri;

    /**
     * 请求IP
     */
    private String ip;

    /**
     * 登录用户ID
     */
    private String loginUserId;
}
