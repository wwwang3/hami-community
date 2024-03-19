package top.wang3.hami.web.handler;

import jakarta.servlet.ServletException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import top.wang3.hami.core.exception.CaptchaServiceException;
import top.wang3.hami.core.exception.HamiServiceException;
import top.wang3.hami.security.exception.NotLoginException;
import top.wang3.hami.security.model.Result;
import top.wang3.hami.security.ratelimit.RateLimitException;

import java.sql.SQLException;


@RestControllerAdvice
@Slf4j
public class ServiceExceptionHandler {


    @ExceptionHandler(value = {CaptchaServiceException.class, HamiServiceException.class, NotLoginException.class})
    public Result<Void> handleCaptchaException(Exception e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            logError(cause, true);
        } else {
            logError(e);
        }
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(value = {RateLimitException.class})
    public Result<Void> handleRateLimitException(RateLimitException e) {
        logError(e, false);
        return Result
                .error(403, e.getMessage());
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

    @ExceptionHandler(value = {SQLException.class, DataAccessException.class})
    public Result<Void> handleSQLException(SQLException e) {
        logError(e, true);
        return Result.error("系统错误");
    }

    @ExceptionHandler(value = {NoHandlerFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleHandlerException(ServletException exception) {
        // 找不到静态资源
        return Result.error(exception.getMessage());
    }

    @ExceptionHandler(value = {NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleResourceException(NoResourceFoundException exception) {
        // 找不到静态资源
        logError(exception);
        return Result.error(exception.getMessage());
    }

    @ExceptionHandler(value = {ServletException.class})
    public Result<Void> handleServletException(ServletException exception) {
        logError(exception);
        return Result.error(exception.getMessage());
    }

    @ExceptionHandler(value = {Exception.class})
    public Result<Void> resolveException(Exception e) {
        logError(e, true);
        return Result.error(e.getMessage());
    }


    private void logError(Throwable e) {
        logError(e, false);
    }


    @SuppressWarnings(value = "all")
    private void logError(Throwable e, boolean printTrace) {
        log.error("resolved exception: [{}: {}]", e.getClass(), e.getMessage());
        if (printTrace) {
            e.printStackTrace();
        }
    }

}
