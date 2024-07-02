package top.wang3.hami.core.service.stat.handler.site;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wang3.hami.canal.CanalEntryHandler;
import top.wang3.hami.canal.annotation.CanalRabbitHandler;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.model.SiteStat;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.cache.CacheService;
import top.wang3.hami.core.service.stat.CountService;

@Component
@RequiredArgsConstructor
@CanalRabbitHandler(value = "tb_site_stat", container = "canal-site-stat-container-1")
public class SitePvStatHandler implements CanalEntryHandler<SiteStat> {
    private final CacheService cacheService;
    private final CountService countService;

    @Override
    public void processInsert(SiteStat entity) {
        // ignore it
    }

    @Override
    public void processUpdate(SiteStat before, SiteStat after) {
        ensureCache();
        refreshExpire();
        int pv = after.getPv() - before.getPv();
        int uv = after.getUv() - before.getUv();
        RedisClient.hIncr(RedisConstants.SITE_STAT, "pv", pv);
        RedisClient.hIncr(RedisConstants.SITE_STAT, "uv", uv);
    }

    @Override
    public void processDelete(SiteStat deletedEntity) {

    }

    private void ensureCache() {
        cacheService.getHashValue(RedisConstants.SITE_STAT,
            "users",
            countService::loadSiteStat,
            TimeoutConstants.DEFAULT_EXPIRE
        );
    }

    private void refreshExpire() {
        RedisClient.pExpire(RedisConstants.SITE_STAT, TimeoutConstants.DEFAULT_EXPIRE);
    }
}
