package top.wang3.hami.web.handler;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.wang3.hami.core.exception.CaptchaServiceException;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.security.model.Result;


@RestControllerAdvice
@Slf4j
public class ServiceExceptionHandler {


    @ExceptionHandler(value = {CaptchaServiceException.class, ServiceException.class})
    public Result<Void> handleCaptchaException(Exception e) {
        logError(e);
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(value = {ValidationException.class, BindException.class})
    public Result<Void> handleParamException(Exception e) {
        logError(e);
        return Result.error(400, "参数错误");
    }


    private void logError(Exception e) {
        log.warn("resolved exception: [{}: {}]", e.getClass(), e.getMessage());
    }
}
