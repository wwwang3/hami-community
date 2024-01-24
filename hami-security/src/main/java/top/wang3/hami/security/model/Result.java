package top.wang3.hami.security.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import top.wang3.hami.common.HamiFactory;

import java.util.Objects;
import java.util.function.Supplier;

public record Result<T>(int code, String msg, T data, @JsonIgnore Checker checker) {

    public static final ObjectMapper MAPPER = HamiFactory.MAPPER;

    public Result<T> orElse(Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        if (checker != null && checker.check()) {
            return Result.successData(supplier.get());
        }
        return this;
    }

    public Result<T> orElse(String errorMsg) {
        if (checker != null && !checker.check()) {
            return Result.error(errorMsg);
        }
        return this;
    }

    public static <T> Result<T> of(T data) {
        Objects.requireNonNull(data);
        return Result.successData(data);
    }

    public static <T> Result<T> ofNullable(T data) {
        return new Result<>(200, "success", data, () -> data != null);
    }

    public static Result<Void> ofTrue(boolean success) {
        return ofTrue(() -> success);
    }


    public static Result<Void> ofTrue(Supplier<Boolean> supplier) {
        Objects.requireNonNull(supplier);
        return new Result<>(200, "success", null, supplier::get);
    }


    public static <T> Result<T> successData(T data) {
        return new Result<>(200, "success", data, null);
    }

    public static <T> Result<T> success(String msg) {
        return new Result<>(200, msg, null, null);
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "success", null, null);
    }

    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null, null);
    }

    public static <T> Result<T> error() {
        return new Result<>(500, "error", null, null);
    }
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null, null);
    }

    @SneakyThrows
    public String toJsonString() {
        return MAPPER.writeValueAsString(this);
    }


    public static <R> Result<R> successIfTrue(boolean r) {
        return successIfTrue(r, "error");
    }

    public static <R> Result<R> successIfTrue(boolean r, String errorMsg) {
        return r ? Result.success() : Result.error(errorMsg);
    }

    public static <R> Result<R> successIfNonNull(R o) {
        return Objects.nonNull(o) ? Result.successData(o) : Result.error("error");
    }

    public interface Checker {
        boolean check();
    }

    public static String writeValueAsString(Object value) {
        try {
           return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            // ignore it
            return null;
        }
    }
}
