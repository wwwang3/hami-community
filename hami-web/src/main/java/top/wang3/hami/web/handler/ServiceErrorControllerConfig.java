package top.wang3.hami.web.handler;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import top.wang3.hami.security.model.Result;

import java.util.Map;

@Configuration
@Slf4j
public class ServiceErrorControllerConfig {


    @Controller
    @RequestMapping("${server.error.path:${error.path:/error}}")
    public static class ServiceErrorController implements ErrorController {

        private final ErrorAttributes errorAttributes;

        @Autowired
        public ServiceErrorController(ErrorAttributes errorAttributes) {
            this.errorAttributes = errorAttributes;
        }

        protected HttpStatus getStatus(HttpServletRequest request) {
            Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
            if (statusCode == null) {
                return HttpStatus.INTERNAL_SERVER_ERROR;
            }
            try {
                return HttpStatus.valueOf(statusCode);
            }
            catch (Exception ex) {
                return HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }

        /**
         * 处理异常
         * @param request HttpServletRequest
         * @param response HttpServletResponse
         */
        @RequestMapping
        @ResponseBody
        public Result<Void> handleError(HttpServletRequest request, HttpServletResponse response) {
            Map<String, Object> attributes = getErrorAttributes(request, getErrorAttributeOptions(request));
            Object e = attributes.get("exception");
            if (e instanceof Exception ex) {
                logError(request, ex);
                String msg = ex.getMessage();
                var status = getStatus(request);
                response.setStatus(status.value());
                return Result.error(status.value(), msg);
            } else {
                String error = attributes.get("error").toString();
                return Result.error(error);
            }
        }

        protected Map<String, Object> getErrorAttributes(HttpServletRequest request, ErrorAttributeOptions options) {
            WebRequest webRequest = new ServletWebRequest(request);
            return this.errorAttributes.getErrorAttributes(webRequest, options);
        }

        protected ErrorAttributeOptions getErrorAttributeOptions(HttpServletRequest request) {
            ErrorAttributeOptions options = ErrorAttributeOptions.defaults();
            options = options.including(ErrorAttributeOptions.Include.EXCEPTION);
            options = options.including(ErrorAttributeOptions.Include.MESSAGE);
            options = options.including(ErrorAttributeOptions.Include.BINDING_ERRORS);
            return options;
        }


        private void logError(HttpServletRequest request, Exception e) {
            String uri = request.getRequestURI();
            String clazz = e != null ? e.getClass().getCanonicalName() : "";
            String msg = e != null ? e.getMessage() : "";
            log.warn("resolved exception, path: [{}] error_class:[{}]: error_msg: [{}]", uri,
                    clazz, msg);

        }


    }
}
