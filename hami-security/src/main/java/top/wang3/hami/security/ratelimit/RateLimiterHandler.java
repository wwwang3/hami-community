package top.wang3.hami.security.ratelimit;

public interface RateLimiterHandler {


    String getSupportedAlgorithm();

    boolean isAllowed(String key, int rate, int capacity);
}
