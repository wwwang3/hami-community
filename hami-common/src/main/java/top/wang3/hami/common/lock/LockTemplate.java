package top.wang3.hami.common.lock;

import java.util.concurrent.Callable;

public interface LockTemplate {

    void execute(String key, Runnable runnable);

    <T> T execute(String key, Callable<T> callable);
}
