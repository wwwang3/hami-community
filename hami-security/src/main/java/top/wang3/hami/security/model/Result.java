package top.wang3.hami.security.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

public record Result<T>(int code, String msg, T data) {


    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
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
}
