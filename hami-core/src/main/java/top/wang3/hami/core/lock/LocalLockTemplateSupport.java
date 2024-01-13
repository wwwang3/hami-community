package top.wang3.hami.core.lock;


import org.springframework.stereotype.Component;
import top.wang3.hami.common.lock.LockTemplate;
import top.wang3.hami.core.exception.HamiServiceException;

import java.util.concurrent.Callable;

@Component
public class LocalLockTemplateSupport implements LockTemplate {

    @Override
    public void execute(String key, Runnable runnable) {
        synchronized (key.intern()) {
            runnable.run();
        }
    }

    @Override
    public <T> T execute(String key, Callable<T> callable) {
        try {
            synchronized (key.intern()) {
                return callable.call();
            }
        } catch (Exception e) {
            throw new HamiServiceException(e.getMessage(), e);
        }
    }
}
