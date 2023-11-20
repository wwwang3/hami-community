package top.wang3.hami.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import top.wang3.hami.security.context.IpContext;
import top.wang3.hami.security.context.LoginUserContext;
import top.wang3.hami.security.model.LoginUser;
import top.wang3.hami.security.model.Result;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * 请求日志记录过滤器
 */
@Slf4j
public class RequestLogFilter extends OncePerRequestFilter {

    private final Set<String> ignores = Set.of("/swagger-ui", "/v3/api-docs");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(this.isIgnoreUrl(request.getServletPath())) {
            filterChain.doFilter(request, response);
        } else {
            long startTime = System.currentTimeMillis();
            this.logRequestStart(request);
            ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
            filterChain.doFilter(request, wrapper);
            this.logRequestEnd(wrapper, startTime);
            wrapper.copyBodyToResponse();
        }
    }

    /**
     * 判定当前请求url是否不需要日志打印
     * @param url 路径
     * @return 是否忽略
     */
    private boolean isIgnoreUrl(String url){
        for (String ignore : ignores) {
            if(url.startsWith(ignore)) return true;
        }
        return false;
    }

    /**
     * 请求结束时的日志打印，包含处理耗时以及响应结果
     * @param wrapper 用于读取响应结果的包装类
     * @param startTime 起始时间
     */
    public void logRequestEnd(ContentCachingResponseWrapper wrapper, long startTime){
        long time = System.currentTimeMillis() - startTime;
        int status = wrapper.getStatus();
        byte[] bytes = wrapper.getContentAsByteArray();
        String content = status != 200 ?
                status + " 错误" : new String(bytes, 0, Math.min(512, bytes.length));
        log.info("请求处理耗时: {}ms | 响应结果: {}", time, content);
    }

    /**
     * 请求开始时的日志打印，包含请求全部信息，以及对应用户角色
     * @param request 请求
     */
    public void logRequestStart(HttpServletRequest request){
        Map<String, String[]> map = request.getParameterMap();
        String object = Result.writeValueAsString(map);
        LoginUser loginUser = LoginUserContext.getLoginUser();
        String ip = IpContext.getIp();
        if(loginUser != null) {
            log.info("请求URL: \"{}\" ({}) | 远程IP地址: {} │ 身份: {} (UID: {}) | 角色: {} | 请求参数列表: {}",
                    request.getServletPath(), request.getMethod(), ip,
                    loginUser.getUsername(), loginUser.getId(), loginUser.getAuthorities(), object);
        } else {
            log.info("请求URL: \"{}\" ({}) | 远程IP地址: {} │ 身份: 未验证 | 请求参数列表: {}",
                    request.getServletPath(), request.getMethod(), ip, object);
        }
    }
}
