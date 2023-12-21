package top.wang3.hami.core.service.interact.handler.count;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wang3.hami.canal.annotation.CanalRabbitHandler;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.model.ArticleCollect;
import top.wang3.hami.core.cache.CacheService;
import top.wang3.hami.core.service.interact.CollectService;


@CanalRabbitHandler(value = "article_collect", container = "canal-interact-container-2")
@Component
@Slf4j
public class CollectCountHandler extends AbstractInteractCountHandler<ArticleCollect> {

    private final CollectService collectService;

    public CollectCountHandler(CacheService cacheService, CollectService collectService) {
        super(cacheService);
        this.collectService = collectService;
    }

    @Override
    public String buildKey(ArticleCollect entity) {
        return RedisConstants.USER_COLLECT_COUNT + entity.getUserId();
    }

    @Override
    public boolean isInsert(ArticleCollect before, ArticleCollect after) {
        return Constants.ONE.equals(after.getState()) && Constants.ZERO.equals(before.getState());
    }

    @Override
    protected void loadCount(ArticleCollect entity) {
        collectService.getUserCollectCount(entity.getUserId());
    }
}
