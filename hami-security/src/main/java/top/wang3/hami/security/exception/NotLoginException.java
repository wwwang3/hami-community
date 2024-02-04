package top.wang3.hami.security.exception;

public class NotLoginException extends RuntimeException {

    public NotLoginException() {
        super("未登录");
    }
}
