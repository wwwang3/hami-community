package top.wang3.hami.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import top.wang3.hami.common.lock.LockTemplate;

public class HamiFactory {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    private static LockTemplate LOCK_TEMPLATE;

    protected static void registerLockTemplate(LockTemplate lockTemplate) {
        HamiFactory.LOCK_TEMPLATE = lockTemplate;
    }

    public static LockTemplate getLockTemplate() {
        return LOCK_TEMPLATE;
    }
}
