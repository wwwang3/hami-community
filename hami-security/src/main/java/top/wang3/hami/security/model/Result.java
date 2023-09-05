package top.wang3.hami.security.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public record Result<T>(int code, String msg, T data, @JsonIgnore Checker checker) {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    public Result<T> orElse(Supplier<Result<T>> supplier) {
        Objects.requireNonNull(supplier);
        if (checker != null && checker.check().get()) {
            return supplier.get();
        }
        return this;
    }

    public Result<T> orElse(String errorMsg) {
        if (checker != null && !checker.check().get()) {
            return Result.error(errorMsg);
        }
        return this;
    }

    public static <T> Result<T> of(T data) {
        Objects.requireNonNull(data);
        return Result.successData(data);
    }

    public static <T> Result<T> ofNullable(T data) {
        return new Result<>(200, "success", data, new NullChecker<>(() -> data));
    }

    public static Result<Void> ofTrue(boolean success) {
        return ofTrue(() -> success);
    }


    public static Result<Void> ofTrue(Supplier<Boolean> supplier) {
        Objects.requireNonNull(supplier);
        return new Result<>(200, "success", null, new BooleanChecker<>(supplier.get()));
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

    public static <R> Result<R> successIfNoNull(Supplier<R> supplier) {
        if (supplier == null) return Result.error("error");
        return Optional
                .ofNullable(supplier.get())
                .map(Result::successData)
                .orElse(Result.error("error"));
    }

    public interface Checker {
        Supplier<Boolean> TRUE = () -> true;
        Supplier<Boolean> FALSE = () -> false;

        Supplier<Boolean> check();

    }

    public static class NullChecker<T> implements Checker {

        Supplier<T> supplier;

        public NullChecker(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public Supplier<Boolean> check() {
            return supplier != null && supplier.get() != null ? TRUE : FALSE;
        }
    }

    public static class BooleanChecker<T> implements Checker {

        boolean success = true;

        public BooleanChecker(boolean success) {
            this.success = success;
        }

        @Override
        public Supplier<Boolean> check() {
            return success ? TRUE : FALSE;
        }

    }

}
