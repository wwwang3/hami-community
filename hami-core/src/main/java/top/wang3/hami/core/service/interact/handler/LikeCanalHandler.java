package top.wang3.hami.core.service.interact.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.annotation.CanalListener;
import top.wang3.hami.common.canal.CanalEntryHandler;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.LikeItem;
import top.wang3.hami.common.util.RandomUtils;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.interact.LikeService;

import java.util.concurrent.TimeUnit;


@Component
@CanalListener(value = "tb_like")
@RequiredArgsConstructor
@Slf4j
public class LikeCanalHandler implements CanalEntryHandler<LikeItem> {

    private final LikeService likeService;

    @Override
    public void processInsert(LikeItem entity) {
        if (Constants.LIKE_TYPE_COMMENT.equals(entity.getItemType())) {
            //用户点赞的评论不存了
            return;
        }
        String key = Constants.LIST_USER_LIKE_ARTICLES + entity.getLikerId();
        //点赞
        //key已经过期或者不存在返回false
        boolean success = RedisClient.expire(key, RandomUtils.randomLong(10, 20), TimeUnit.DAYS);
        if (!success) {
            //缓存过期
            //单个队列单个消费者 非极端情况下能保证消息的顺序性, 这里就不加锁了
            //极端情况下, 缓存刚好过期, 来了一个查询请求, 查询请求读时没有缓存, 去加载, 然后有个点赞请求点赞成功, 投递了消息
            //查询请求读的是[1, 2] 点赞成功后读的是[1, 2, 3] 然后这里先更新了, 读请求再更新, 导致存的是旧的
            //读请求 ===>无缓存 ==> 读DB ===> 写Redis
            //写请求 ===> 写DB ===> 发消息 ===>写Redis
            likeService.loadUserLikeArticleCache(key, entity.getLikerId(), 0, 0);
        } else {
            //todo 消费失败先不管 _(≧∇≦」∠)_
            RedisClient.zAdd(key, entity.getItemId(), entity.getMtime().getTime());
        }
    }

    @Override
    public void processUpdate(LikeItem before, LikeItem after) {
        Byte oldState = before.getState();
        Byte state = after.getState();
        log.debug("like: before: {}, after: {}", before, after);
        if (Constants.ZERO.equals(oldState) && Constants.ONE.equals(state)) {
            //点赞
            processInsert(after);
        } else {
            processDelete(after);
        }
    }

    @Override
    public void processDelete(LikeItem deletedEntity) {
        if (Constants.LIKE_TYPE_COMMENT.equals(deletedEntity.getItemType())) {
            //用户点赞的评论不存了
            return;
        }
        String key = Constants.LIST_USER_LIKE_ARTICLES + deletedEntity.getLikerId();
        //删除
        RedisClient.zRem(key, deletedEntity.getItemId());
    }
}
