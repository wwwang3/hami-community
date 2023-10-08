package top.wang3.hami.core.service.interact.handler;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.annotation.CanalListener;
import top.wang3.hami.common.canal.CanalEntryHandler;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.ArticleCollect;
import top.wang3.hami.common.util.RandomUtils;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.component.ZPageHandler;
import top.wang3.hami.core.service.interact.CollectService;

import java.util.concurrent.TimeUnit;

@Component
@CanalListener("article_collect")
@RequiredArgsConstructor
public class CollectCanalHandler implements CanalEntryHandler<ArticleCollect> {


    //用户收藏Canal消息处理器
    private final CollectService collectService;

    @Override
    public void processInsert(ArticleCollect entity) {
        String redisKey = Constants.LIST_USER_COLLECT + entity.getUserId();
        boolean success = RedisClient.expire(redisKey, RandomUtils.randomLong(10, 20), TimeUnit.HOURS);
        if (success && RedisClient.zCard(redisKey) < ZPageHandler.DEFAULT_MAX_SIZE) {
            RedisClient.zAdd(redisKey, entity.getArticleId(), entity.getMtime().getTime());
        } else {
            collectService.loadUserCollects(redisKey, entity.getUserId(), -1, -1);
        }
        deleteCountCache(entity.getUserId());
    }

    @Override
    public void processUpdate(ArticleCollect before, ArticleCollect after) {
        Byte oldState = before.getState();
        Byte state = after.getState();
        if (Constants.ZERO.equals(oldState) && Constants.ONE.equals(state)) {
            //点赞
            processInsert(after);
        } else {
            processDelete(after);
        }
    }

    @Override
    public void processDelete(ArticleCollect deletedEntity) {
        String redisKey = Constants.LIST_USER_COLLECT + deletedEntity.getUserId();
        RedisClient.zRem(redisKey, deletedEntity.getArticleId());
        deleteCountCache(deletedEntity.getUserId());
    }

    private void deleteCountCache(Integer userId) {
        String userCollectCountKey = Constants.USER_COLLECT_COUNT + userId;
        RedisClient.deleteObject(userCollectCountKey);
    }

}
