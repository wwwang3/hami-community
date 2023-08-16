package top.wang3.hami.security.listener;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.wang3.hami.common.dto.IpInfo;
import top.wang3.hami.common.util.IpUtils;
import top.wang3.hami.security.handler.AuthenticationEventHandler;
import top.wang3.hami.security.model.LoginUser;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class AuthenticationEventListener {

    private final AuthenticationEventHandler handler;

    public AuthenticationEventListener(AuthenticationEventHandler handler) {
        this.handler = handler;
    }

    public void init() {
        log.debug("listener AuthenticationEventListener register for use");
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        Authentication authentication = success.getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        IpInfo ipInfo = (attributes == null) ? null : IpUtils.getIpInfo(attributes.getRequest());
        //async
        CompletableFuture.runAsync(() -> handler.handleSuccess(loginUser, ipInfo, new Date()));
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failures) {
        // ...
    }
}
