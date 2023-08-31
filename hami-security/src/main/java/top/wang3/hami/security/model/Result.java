package top.wang3.hami.security.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public record Result<T>(int code, String msg, T data) {


    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> successData(T data) {
        return new Result<>(200, "success", data);
    }
    public static <T> Result<T> success(String msg) {
        return new Result<>(200, msg, null);
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
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
        return Objects.nonNull(o) ? Result.success(o) : Result.error("error");
    }

    public static <R> Result<R> successIfNoNull(Supplier<R> supplier) {
        if (supplier == null) return Result.error("error");
        return Optional
                .ofNullable(supplier.get())
                .map(Result::successData)
                .orElse(Result.error("error"));
    }
}
