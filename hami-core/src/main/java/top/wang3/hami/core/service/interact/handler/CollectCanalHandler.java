package top.wang3.hami.core.service.interact.handler;


import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.annotation.CanalListener;
import top.wang3.hami.common.canal.CanalEntryHandler;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.ArticleCollect;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.like.LikeService;

@Component
@CanalListener("article_collect")
public class CollectCanalHandler implements CanalEntryHandler<ArticleCollect> {

    @Resource
    LikeService likeService;

    @Override
    public void processInsert(ArticleCollect entity) {
        String redisKey = Constants.LIST_USER_COLLECT + entity.getUserId();
        RedisClient.zAdd(redisKey, entity.getArticleId(), entity.getMtime().getTime());
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
    }
}