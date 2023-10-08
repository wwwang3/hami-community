package top.wang3.hami.core.service.stat.handler;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.annotation.CanalListener;
import top.wang3.hami.common.canal.CanalEntryHandler;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.util.RedisClient;

@Component
@CanalListener(value = "article_stat")
@Slf4j
//todo 失败重试
public class UserStatHandler implements CanalEntryHandler<ArticleStat> {


    @Override
    public void processInsert(ArticleStat entity) {
        deleteUserStatCache(entity.getUserId());
    }

    @Override
    public void processUpdate(ArticleStat before, ArticleStat after) {
        deleteUserStatCache(after.getUserId());
    }

    @Override
    public void processDelete(ArticleStat deletedEntity) {
        deleteUserStatCache(deletedEntity.getUserId());
    }

    private void deleteUserStatCache(Integer userId) {
        RedisClient.deleteObject(Constants.STAT_TYPE_USER + userId);
    }
}
