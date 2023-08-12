package top.wang3.hami.mail.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import top.wang3.hami.mail.sender.CustomMailSender;
import top.wang3.hami.mail.service.MailSenderService;
import top.wang3.hami.mail.supplier.MailSenderSupplier;
import top.wang3.hami.mail.supplier.RandomMailSenderSupplier;
import top.wang3.hami.mail.supplier.RoundRobinMailSenderSupplier;

import java.util.*;

/**
 * 多Mail源自动配置类
 */
@AutoConfiguration
@EnableConfigurationProperties({CustomMailProperties.class, MailProperties.class})
@Slf4j
public class CustomMailAutoConfiguration {

    private final CustomMailProperties customMailProperties;

    public CustomMailAutoConfiguration(CustomMailProperties customMailProperties) {
        this.customMailProperties = customMailProperties;
    }

    @Bean("mailServiceList")
    public List<CustomMailSender> mailServiceList() {
        var configs = customMailProperties.getConfigs();
        var senders = new ArrayList<CustomMailSender>();
        var set = new HashSet<String>();
        configs.forEach(config -> {
            if (set.contains(config.getKey())) {
                throw new IllegalStateException("key can not repeat");
            }
            set.add(config.getKey());
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            applyProperties(config, sender);
            senders.add(new CustomMailSender(config.getKey(), sender));
        });
        log.info("load {} mail-services", senders.size());
        return senders;
    }

    @Bean("mailSenderService")
    public MailSenderService mailSenderService(MailSenderSupplier handler, CustomMailProperties properties) {
        return new MailSenderService(handler, properties);
    }

    @Bean("mailHandler")
    public MailSenderSupplier mailHandler(List<CustomMailSender> senders, CustomMailProperties properties) {
        CustomMailProperties.Strategy strategy = properties.getStrategy();
        MailSenderSupplier handler = switch (strategy) {
            case RANDOM -> new RandomMailSenderSupplier(senders);
            case ROUND_ROBIN -> new RoundRobinMailSenderSupplier(senders);
        };
        handler.setRetry(properties.isRetry());
        return handler;
    }

    private void applyProperties(CustomMailProperties.MailConfig properties, JavaMailSenderImpl sender) {
        sender.setHost(properties.getHost());
        if (properties.getPort() != null) {
            sender.setPort(properties.getPort());
        }
        sender.setUsername(properties.getUsername());
        sender.setPassword(properties.getPassword());
        sender.setProtocol(properties.getProtocol());
        if (properties.getDefaultEncoding() != null) {
            sender.setDefaultEncoding(properties.getDefaultEncoding().name());
        }
        if (!properties.getProperties().isEmpty()) {
            sender.setJavaMailProperties(asProperties(properties.getProperties()));
        }
    }

    private Properties asProperties(Map<String, String> source) {
        Properties properties = new Properties();
        properties.putAll(source);
        return properties;
    }
}
