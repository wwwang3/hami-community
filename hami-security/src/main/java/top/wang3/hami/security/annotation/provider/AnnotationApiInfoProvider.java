package top.wang3.hami.security.annotation.provider;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import top.wang3.hami.security.annotation.Api;
import top.wang3.hami.security.annotation.ApiInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 从{@link top.wang3.hami.security.annotation.Api}获取ApiInfo
 */
@Component
@Order(2)
public class AnnotationApiInfoProvider implements ApiInfoProvider{

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    public AnnotationApiInfoProvider(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @Override
    public List<ApiInfo> getApis() {
        // 获取handlerMethod
        LinkedList<ApiInfo> apis = new LinkedList<>();
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            resolveApiInfo(entry, apis);
        }
        return apis;
    }

    private void resolveApiInfo(Map.Entry<RequestMappingInfo, HandlerMethod> entry, List<ApiInfo> apis) {
        RequestMappingInfo info = entry.getKey();
        HandlerMethod handlerMethod = entry.getValue();
        Api api = handlerMethod.getMethodAnnotation(Api.class);
        if (api == null) {
            // 从Controller类上寻找
            api = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), Api.class);
        }
        // 没有
        if (api == null) return;
        apis.add(buildApiInfo(info.getPatternValues(), api));
    }

    private ApiInfo buildApiInfo(Set<String> patternValues, Api api) {
        ApiInfo info = new ApiInfo();
        info.setAccessControl(api.access());
        info.setRoles(api.roles());
        info.setAuthorities(api.authorities());
        info.setHttpMethod(!StringUtils.hasText(api.httpMethod()) ? null : HttpMethod.valueOf(api.httpMethod()));
        info.setPatterns(patternValues.toArray(new String[0]));
        return info;
    }
}
