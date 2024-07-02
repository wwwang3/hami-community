package top.wang3.hami.core.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import top.wang3.hami.common.message.PageRequestLogMessage;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.security.context.IpContext;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("all")
public class PvRecordFilter extends OncePerRequestFilter implements Ordered {

    private final RabbitMessagePublisher rabbitMessagePublisher;

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
       if (!request.getRequestURI().startsWith("/api/v1/admin")) {
           String ip = IpContext.getIp();
           PageRequestLogMessage message = new PageRequestLogMessage(ip);
           rabbitMessagePublisher.publishMsg(message);
       }
        filterChain.doFilter(request, response);
    }
}
