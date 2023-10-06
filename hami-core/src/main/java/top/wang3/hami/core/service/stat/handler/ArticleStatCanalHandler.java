package top.wang3.hami.core.service.stat.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.annotation.CanalListener;
import top.wang3.hami.common.canal.CanalEntryHandler;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.converter.ArticleConverter;
import top.wang3.hami.common.dto.article.ArticleStatDTO;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.util.RandomUtils;
import top.wang3.hami.common.util.RedisClient;

import java.util.concurrent.TimeUnit;


@Component
@CanalListener(value = "article_stat")
@Slf4j
//todo 失败重试
public class ArticleStatCanalHandler implements CanalEntryHandler<ArticleStat> {

    @Override
    public void processInsert(ArticleStat entity) {
        if (entity == null) return;
        //在文章数据表插入了一条数据
        //写入Redis
        String redisKey = Constants.COUNT_TYPE_ARTICLE + entity.getArticleId();
        ArticleStatDTO dto = ArticleConverter.INSTANCE.toArticleStatDTO(entity);
        RedisClient.setCacheObject(redisKey, dto, RandomUtils.randomLong(10, 20), TimeUnit.HOURS);
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
