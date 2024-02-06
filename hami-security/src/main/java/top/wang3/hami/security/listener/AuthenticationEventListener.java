package top.wang3.hami.security.listener;


import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import top.wang3.hami.common.dto.IpInfo;
import top.wang3.hami.common.message.user.LoginRabbitMessage;
import top.wang3.hami.security.context.IpContext;
import top.wang3.hami.security.model.LoginUser;

import java.util.Date;

@Slf4j
@Setter
public class AuthenticationEventListener {

    private RabbitTemplate rabbitTemplate;

    public void init() {
        log.info("listener AuthenticationEventListener register for use");
    }

    @Async
    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        Authentication authentication = success.getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        IpInfo info = IpContext.getIpInfo();
        LoginRabbitMessage message = new LoginRabbitMessage(loginUser.getId(), info, new Date().getTime());
        // 发布登录成功消息
        rabbitTemplate.convertAndSend(message.getExchange(), message.getRoute(), message);
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failures) {
        // ...
        if (log.isDebugEnabled()) {
            AuthenticationException exception = failures.getException();
            log.debug("auth failed, error_class: {}, error_msg: {}",
                    exception.getClass().getSimpleName(), exception.getMessage());
        }
    }
}
