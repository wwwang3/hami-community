package top.wang3.hami.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import top.wang3.hami.common.lock.LockTemplate;

public class HamiFactory {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static LockTemplate LOCK_TEMPLATE;

    private static ThreadPoolTaskExecutor TASK_EXECUTOR;

    protected static void registerLockTemplate(LockTemplate lockTemplate) {
        HamiFactory.LOCK_TEMPLATE = lockTemplate;
    }

    protected static void registerTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
        HamiFactory.TASK_EXECUTOR = taskExecutor;
    }

    public static LockTemplate getLockTemplate() {
        return LOCK_TEMPLATE;
    }

    public static ThreadPoolTaskExecutor getTaskExecutor() {
        return TASK_EXECUTOR;
    }

    public static ObjectMapper getObjectMapper() {
        return MAPPER;
    }
}
