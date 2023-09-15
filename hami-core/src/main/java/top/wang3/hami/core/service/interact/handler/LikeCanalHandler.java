package top.wang3.hami.core.service.interact.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.annotation.CanalListener;
import top.wang3.hami.common.canal.CanalEntryHandler;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.LikeItem;
import top.wang3.hami.common.util.RedisClient;


@Component
@CanalListener(value = "tb_like")
@Slf4j
public class LikeCanalHandler implements CanalEntryHandler<LikeItem> {

    @Override
    public void processInsert(LikeItem entity) {
        //点赞
        Integer likerId = entity.getLikerId();
        Byte itemType = entity.getItemType();
        String redisKey = Constants.LIST_USER_LIKE + likerId + "-" + itemType;
        RedisClient.zAdd(redisKey, entity.getItemId(), entity.getMtime().getTime());
    }

    @Override
    public void processUpdate(LikeItem before, LikeItem after) {
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
    public void processDelete(LikeItem deletedEntity) {
        Integer likerId = deletedEntity.getLikerId();
        Byte itemType = deletedEntity.getItemType();
        String redisKey = Constants.LIST_USER_LIKE + likerId + "-" + itemType;
        RedisClient.zRem(redisKey, deletedEntity.getItemId());
    }
}
