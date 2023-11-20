package top.wang3.hami.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import top.wang3.hami.common.component.SnowflakeIdGenerator;
import top.wang3.hami.common.constant.Constants;

import java.io.IOException;

@Setter
public class RequestIDFilter extends OncePerRequestFilter {


    SnowflakeIdGenerator generator;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            MDC.put(Constants.REQ_ID, String.valueOf(generator.nextId()));
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
