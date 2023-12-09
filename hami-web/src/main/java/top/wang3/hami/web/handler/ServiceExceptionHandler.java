package top.wang3.hami.web.handler;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
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
        logError(e);
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(value = {RateLimitException.class})
    public Result<Void> handleRateLimitException(RateLimitException e) {
        logError(e, true);
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

    @ExceptionHandler(value = {Exception.class})
    public Result<Void> resolveException(Exception e) {
        logError(e, true);
        return Result.error(e.getMessage());
    }


    private void logError(Exception e) {
        logError(e, false);
    }


    @SuppressWarnings(value = "all")
    private void logError(Exception e, boolean printTrace) {
        log.error("resolved exception: [{}: {}]", e.getClass(), e.getMessage());
        if (printTrace) {
            e.printStackTrace();
        }
    }

}
