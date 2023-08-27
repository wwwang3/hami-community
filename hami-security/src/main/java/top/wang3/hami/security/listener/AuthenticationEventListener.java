package top.wang3.hami.security.listener;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import top.wang3.hami.common.dto.IpInfo;
import top.wang3.hami.security.context.IpContext;
import top.wang3.hami.security.handler.AuthenticationEventHandler;
import top.wang3.hami.security.model.LoginUser;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
public class AuthenticationEventListener {

    private final AuthenticationEventHandler handler;

    Executor executor = Executors.newFixedThreadPool(4);

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
        //从ThreadLocal中获取
        IpInfo info = IpContext.getIpInfo();
        //async
        CompletableFuture.runAsync(() -> handler.handleSuccess(loginUser, info, new Date()), executor);
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failures) {
        // ...
    }
}
