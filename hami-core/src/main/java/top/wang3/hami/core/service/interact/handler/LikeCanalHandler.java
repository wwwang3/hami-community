package top.wang3.hami.core.service.interact.handler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import top.wang3.hami.canal.CanalEntryHandler;
import top.wang3.hami.canal.annotation.CanalRabbitHandler;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.model.LikeItem;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.interact.LikeService;


@Component
@CanalRabbitHandler(value = "tb_like")
@RequiredArgsConstructor
@Slf4j
public class LikeCanalHandler implements CanalEntryHandler<LikeItem> {

    private final LikeService likeService;

    private RedisScript<Long> likeScript;

    @PostConstruct
    public void init() {
        likeScript = RedisClient.loadScript("/META-INF/scripts/like.lua");
    }

    @Override
    public void processInsert(LikeItem entity) {
//        String key = RedisConstants.USER_LIKE_LIST + entity.getItemType() + ":" + entity.getLikerId();
//        //点赞
//        //key已经过期或者不存在返回false
//        boolean success = RedisClient.expire(key, RandomUtils.randomLong(10, 20), TimeUnit.DAYS);
//        if (!success) {
//            //单个队列单个消费者 非极端情况下能保证消息的顺序性, 这里就不加锁了
//            //极端情况下, 缓存刚好过期, 来了一个查询请求, 查询请求读时没有缓存, 去加载, 然后有个点赞请求点赞成功, 投递了消息
//            //查询请求读的是[1, 2] 点赞成功后读的是[1, 2, 3] 然后这里先更新了, 读请求再更新, 导致存的是旧的
//            //读请求 ===>无缓存 ==> 读DB ===> 写Redis
//            //写请求 ===> 写DB ===> 发消息 ===>写Redis
//            likeService.loadUserLikeItem(key, entity.getLikerId(),
//                    LikeType.of(entity.getItemType()), -1, -1);
//        } else {
//            Integer member = entity.getItemId();
//            Long score = entity.getMtime().getTime();
//            Long res = RedisClient.executeScript(likeScript, List.of(key),
//                    List.of(member, score, ZPageHandler.DEFAULT_MAX_SIZE));
//            log.info("like--userId: {}, itemId: {}, res: {}", entity.getLikerId(), score, res);
//        }
    }

    @Override
    public void processUpdate(LikeItem before, LikeItem after) {
//        Byte oldState = before.getState();
//        Byte state = after.getState();
//        log.debug("like: before: {}, after: {}", before, after);
//        if (Constants.ZERO.equals(oldState) && Constants.ONE.equals(state)) {
//            //点赞
//            processInsert(after);
//        } else {
//            handleDelete(after);
//        }
    }

    @Override
    public void processDelete(LikeItem deletedEntity) {
        handleDelete(deletedEntity);
        deleteCountCache(deletedEntity.getLikerId(), deletedEntity.getItemType());
    }

    private void handleDelete(LikeItem item) {
        //删除
        String key = RedisConstants.USER_LIKE_LIST + item.getItemType() + ":" + item.getLikerId();
        RedisClient.zRem(key, item.getItemId());
    }

    public void deleteCountCache(Integer likerId, Byte type) {
        //删除用户点赞数缓存(文章或评论被删除情况)
        String userLikeCountKey = RedisConstants.USER_LIKE_COUNT + type + ":" + likerId;
        RedisClient.deleteObject(userLikeCountKey);
    }
}
