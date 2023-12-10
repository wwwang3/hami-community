package top.wang3.hami.core.service.interact.handler;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.canal.CanalEntryHandler;
import top.wang3.hami.common.canal.annotation.CanalListener;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.model.ArticleCollect;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.interact.CollectService;

@Component
@CanalListener("article_collect")
@RequiredArgsConstructor
@Slf4j
public class CollectCanalHandler implements CanalEntryHandler<ArticleCollect> {


    //用户收藏Canal消息处理器
    private final CollectService collectService;

    private RedisScript<Long> collectScript;

    @PostConstruct
    public void init() {
        collectScript = RedisClient.loadScript("/META-INF/scripts/collect.lua");
    }

    @Override
    public void processInsert(ArticleCollect entity) {
//        String redisKey = RedisConstants.LIST_USER_COLLECT + entity.getUserId();
//        boolean success = RedisClient.expire(redisKey, RandomUtils.randomLong(10, 20), TimeUnit.HOURS);
//        if (!success) {
//            collectService.loadUserCollects(redisKey, entity.getUserId(), -1, -1);
//        } else {
//            Integer member = entity.getArticleId();
//            Long score = entity.getMtime().getTime();
//            Long res = RedisClient.executeScript(collectScript, List.of(redisKey),
//                    List.of(member, score, ZPageHandler.DEFAULT_MAX_SIZE));
//            log.info("collect--userId: {}, articleId: {}, res: {}", entity.getUserId(), score, res);
//        }
    }

    @Override
    public void processUpdate(ArticleCollect before, ArticleCollect after) {
//        Byte oldState = before.getState();
//        Byte state = after.getState();
//        if (Constants.ZERO.equals(oldState) && Constants.ONE.equals(state)) {
//            //点赞
//            processInsert(after);
//        } else {
//            handleDelete(after);
//        }
    }

    @Override
    public void processDelete(ArticleCollect deletedEntity) {
        handleDelete(deletedEntity);
        deleteCountCache(deletedEntity.getUserId());
    }

    private void handleDelete(ArticleCollect entity) {
        String redisKey = RedisConstants.LIST_USER_COLLECT + entity.getUserId();
        RedisClient.zRem(redisKey, entity.getArticleId());
    }

    private void deleteCountCache(Integer userId) {
        String userCollectCountKey = RedisConstants.USER_COLLECT_COUNT + userId;
        RedisClient.deleteObject(userCollectCountKey);
    }

}
