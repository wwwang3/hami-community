package top.wang3.hami.mail.config;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "hami.mail")
@Data
public class CustomMailProperties {

    /**
     * 每个MailSender的配置
     */
    @NestedConfigurationProperty
    private List<MailConfig> configs;

    /**
     * 发送失败是否使用其他MailSender重试
     */
    private boolean retry = false;

    /**
     * 发送策略
     */
    private Strategy strategy = Strategy.RANDOM;

    /**
     * 策略枚举类
     */
    public enum Strategy {
        /**
         * 使用随机的MailSender发送
         */
        RANDOM,

        /**
         * 轮询发送
         */
        ROUND_ROBIN
    }

    /**
     * MailSender配置
     */
    @Data
    public static class MailConfig implements InitializingBean {

        public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

        /**
         * 邮件服务器主机
         */
        private String host;

        /**
         * 端口
         */
        private Integer port;

        /**
         * 用户名
         */
        private String username;

        /**
         * 密码(授权码)
         */
        private String password;

        /**
         * 每个Sender的唯一标识
         */
        private String key;

        /**
         * 编码
         */
        private Charset defaultEncoding = DEFAULT_CHARSET;

        /**
         * 协议
         */
        private String protocol = "smtp";

        /**
         * 其他 JavaMail 会话属性
         */
        private final Map<String, String> properties = new HashMap<>();


        @Override
        public void afterPropertiesSet() throws Exception {
            if (key == null || key.isEmpty()) {
                throw new IllegalArgumentException("key cannot be empty");
            }
        }
    }
}
