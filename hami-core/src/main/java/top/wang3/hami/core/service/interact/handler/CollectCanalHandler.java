package top.wang3.hami.core.service.interact.handler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wang3.hami.canal.CanalEntryHandler;
import top.wang3.hami.canal.annotation.CanalRabbitHandler;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.model.ArticleCollect;
import top.wang3.hami.common.util.RedisClient;

@Component
@CanalRabbitHandler(value = "article_collect", container = "canal-interact-container-1")
@RequiredArgsConstructor
@Slf4j
public class CollectCanalHandler implements CanalEntryHandler<ArticleCollect> {

    @Override
    public void processInsert(ArticleCollect entity) {
    }

    @Override
    public void processUpdate(ArticleCollect before, ArticleCollect after) {

    }

    @Override
    public void processDelete(ArticleCollect deletedEntity) {
        handleDelete(deletedEntity);
    }

    private void handleDelete(ArticleCollect entity) {
        String redisKey = RedisConstants.USER_COLLECT_LIST + entity.getUserId();
        RedisClient.zRem(redisKey, entity.getArticleId());
    }


}
