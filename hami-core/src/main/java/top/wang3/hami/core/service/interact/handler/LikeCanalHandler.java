package top.wang3.hami.core.service.interact.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wang3.hami.canal.CanalEntryHandler;
import top.wang3.hami.canal.annotation.CanalRabbitHandler;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.model.LikeItem;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.interact.LikeService;


@Component
@CanalRabbitHandler(value = "tb_like", container = "canal-interact-container-1")
@RequiredArgsConstructor
@Slf4j
public class LikeCanalHandler implements CanalEntryHandler<LikeItem> {

    private final LikeService likeService;


    @Override
    public void processInsert(LikeItem entity) {
    }

    @Override
    public void processUpdate(LikeItem before, LikeItem after) {

    }

    @Override
    public void processDelete(LikeItem deletedEntity) {
        handleDelete(deletedEntity);
    }

    private void handleDelete(LikeItem item) {
        //删除
        String key = RedisConstants.USER_LIKE_LIST + item.getItemType() + ":" + item.getLikerId();
        RedisClient.zRem(key, item.getItemId());
    }

}
