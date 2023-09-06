package top.wang3.hami.web.handler;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import top.wang3.hami.core.exception.CaptchaServiceException;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.security.exception.NotLoginException;
import top.wang3.hami.security.model.Result;


@RestControllerAdvice
@Slf4j
public class ServiceExceptionHandler {


    @ExceptionHandler(value = {CaptchaServiceException.class, ServiceException.class, NotLoginException.class})
    public Result<Void> handleCaptchaException(Exception e) {
        logError(e);
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(value = {ValidationException.class, BindException.class})
    public Result<Void> handleParamException(Exception e) {
        logError(e);
        return Result.error(400, "参数错误");
    }

    @ExceptionHandler(value = {MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public Result<Void> handleMethodArgsException(Exception e) {
        logError(e);
        return Result.error("参数错误");
    }

    @ExceptionHandler(value = {Exception.class})
    public Result<Void> resolveException(Exception e) {
        e.printStackTrace();
        logError(e);
        return Result.error(e.getMessage());
    }


    private void logError(Exception e) {
        log.warn("resolved exception: [{}: {}]", e.getClass(), e.getMessage());
    }
}
