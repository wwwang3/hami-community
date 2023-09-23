package top.wang3.hami.core.service.stat.handler;

import lombok.extern.slf4j.Slf4j;
import top.wang3.hami.common.annotation.CanalListener;
import top.wang3.hami.common.canal.CanalEntryHandler;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.converter.ArticleConverter;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.util.RedisClient;



@CanalListener(value = "article_stat")
@Slf4j
public class ArticleStatCanalHandler implements CanalEntryHandler<ArticleStat> {

    @Override
    public void processInsert(ArticleStat entity) {
        if (entity == null) return;
        //在文章数据表插入了一条数据
        //写入Redis
        //采用先更新MySQL后更新Redis的策略 (shabi想法^_^)
        //读取不到缓存时认为数据库也没有数据
        //数据量小的时候随便怎么玩, 直接查询MySQL都行
        //数据量较大的情况最好还是Cache-Aside模式, 预热数据到Redis
        String redisKey = Constants.COUNT_TYPE_ARTICLE + entity.getArticleId();
        RedisClient.setCacheObject(redisKey, ArticleConverter.INSTANCE.toArticleStatDTO(entity));
        log.debug("insert to Redis success: {}", entity);
    }

    @Override
    public void processUpdate(ArticleStat before, ArticleStat after) {
        //更新
        String redisKey = Constants.COUNT_TYPE_ARTICLE + after.getArticleId();
        RedisClient.setCacheObject(redisKey, ArticleConverter.INSTANCE.toArticleStatDTO(after));
        log.debug("update to Redis success: before: {}, after: {}", before, after);
    }

    @Override
    public void processDelete(ArticleStat deletedEntity) {
        Integer articleId = deletedEntity.getArticleId();
        RedisClient.deleteObject(Constants.COUNT_TYPE_ARTICLE + articleId);
        log.debug("update to Redis success: deleted: {}", deletedEntity);
    }
}
