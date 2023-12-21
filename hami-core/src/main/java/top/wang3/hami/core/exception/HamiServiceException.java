package top.wang3.hami.core.exception;

public class HamiServiceException extends RuntimeException {

    public HamiServiceException(String message) {
        super(message);
    }

    public HamiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
