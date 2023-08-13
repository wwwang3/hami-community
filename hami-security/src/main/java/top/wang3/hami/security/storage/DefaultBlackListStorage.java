package top.wang3.hami.security.storage;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DefaultBlackListStorage implements BlacklistStorage {


    private static final ConcurrentHashMap<String, Long> blackList = new ConcurrentHashMap<>(64);

    ScheduledExecutorService service = new ScheduledThreadPoolExecutor(1);

    public DefaultBlackListStorage() {}

    @Override
    public boolean add(String jwtId, long expireAt) {
        log.debug("add jwtId: {}", jwtId);
        return blackList.putIfAbsent(jwtId, expireAt) == null;
    }

    @Override
    public boolean contains(String jwtId) {
        return blackList.containsKey(jwtId);
    }

    /**
     * 自动清理
     */
    @PostConstruct
    private void autoClear() {
        log.debug("Start periodic cleaning of expired tokens");
        service.schedule(() -> {
            Iterator<Map.Entry<String, Long>> iterator = blackList.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Long> entry = iterator.next();
                Long val = entry.getValue();
                //过期时间小于现在的时间
                if (val <= System.currentTimeMillis()) {
                    log.debug("removed expired jwt: {}", entry.getKey());
                    iterator.remove();
                }
            }
        }, 5, TimeUnit.SECONDS);
    }
}
