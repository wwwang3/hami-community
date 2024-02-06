package top.wang3.hami.core.service.interact.handler.count;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wang3.hami.canal.annotation.CanalRabbitHandler;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.dto.interact.LikeType;
import top.wang3.hami.common.model.LikeItem;
import top.wang3.hami.core.cache.CacheService;
import top.wang3.hami.core.service.interact.LikeService;


@CanalRabbitHandler(value = "tb_like", container = "canal-interact-container-2")
@Component
@Slf4j
public class LikeCountHandler extends AbstractInteractCountHandler<LikeItem> {

    private final LikeService likeService;

    public LikeCountHandler(CacheService cacheService, LikeService likeService) {
        super(cacheService);
        this.likeService = likeService;
    }

    @Override
    public String buildKey(LikeItem entity) {
        return RedisConstants.USER_LIKE_COUNT + entity.getItemType() + ":" + entity.getLikerId();
    }

    @Override
    public boolean isInsert(LikeItem before, LikeItem after) {
        return Constants.ONE.equals(after.getState()) && Constants.ZERO.equals(before.getState());
    }

    @Override
    @SuppressWarnings("all")
    protected void execute(LikeItem entity, int delta) {
        super.execute(entity, delta);
    }

    @Override
    protected void loadCount(LikeItem entity) {
        LikeType likeType = LikeType.of(entity.getItemType());
        likeService.getUserLikeCount(entity.getLikerId(), likeType);
    }
}
