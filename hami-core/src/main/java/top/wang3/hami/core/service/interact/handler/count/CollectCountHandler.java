package top.wang3.hami.core.service.interact.handler.count;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wang3.hami.canal.annotation.CanalRabbitHandler;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.model.ArticleCollect;
import top.wang3.hami.core.cache.CacheService;
import top.wang3.hami.core.service.interact.CollectService;


@Slf4j
@Component
@CanalRabbitHandler(value = "article_collect", container = "canal-interact-container-2")
public class CollectCountHandler extends AbstractInteractCountHandler<ArticleCollect> {

    private final CollectService collectService;

    public CollectCountHandler(CacheService cacheService, CollectService collectService) {
        super(cacheService);
        this.collectService = collectService;
    }

    @Override
    public String buildKey(ArticleCollect entity) {
        return RedisConstants.USER_INTERACT_COUNT_HASH + entity.getUserId();
    }

    @Override
    public String buildHkey(ArticleCollect entity) {
        return RedisConstants.COLLECT_INTERACT_HKEY;
    }

    @Override
    public boolean isInsert(ArticleCollect before, ArticleCollect after) {
        return Constants.ZERO.equals(before.getState()) && Constants.ONE.equals(after.getState());
    }

    @Override
    protected void loadCount(ArticleCollect entity) {
        collectService.getUserCollectCount(entity.getUserId());
    }
}
