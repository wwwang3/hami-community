package top.wang3.hami.core.service.article.handler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import top.wang3.hami.canal.CanalEntryHandler;
import top.wang3.hami.canal.annotation.CanalRabbitHandler;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.lock.LockTemplate;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.common.util.ZPageHandler;
import top.wang3.hami.core.service.article.cache.ArticleCacheService;

import java.util.List;
import java.util.Objects;


@Component
@CanalRabbitHandler(value = "article", container = "canal-article-container-2")
@RequiredArgsConstructor
@Slf4j
public class CateArticleListHandler implements CanalEntryHandler<Article> {

    private final ArticleCacheService articleCacheService;
    private final LockTemplate lockTemplate;

    private RedisScript<Long> insert_article_script;

    @PostConstruct
    public void init() {
        insert_article_script = RedisClient.loadScript("scripts/insert_article_list.lua");
    }


    @Override
    public void processInsert(Article entity) {
        Integer id = entity.getId();
        Integer cateId = entity.getCategoryId();
        String key = buildKey(cateId);
        long timeout = TimeoutConstants.ARTICLE_LIST_EXPIRE;
        Long result = RedisClient.executeScript(
                insert_article_script,
                List.of(key),
                List.of(id, entity.getCtime().getTime(), timeout, ZPageHandler.DEFAULT_MAX_SIZE)
        );
        log.info("article inserted, add to redis-cate-article-list, article: {}", entity);
        if (result == null || result == 0) {
            // 缓存过期
            lockTemplate.execute(RedisConstants.ARTICLE_LIST, () -> {
                articleCacheService.loadArticleListCache(null);
            });
        }
    }

    @Override
    public void processUpdate(Article before, Article after) {
        if (isLogicDelete(before.getDeleted(), after.getDeleted())) {
            processDelete(after);
        } else {
            if (Objects.equals(before.getCategoryId(), after.getCategoryId())) {
                return;
            }
            // 分类ID改变, 插入到新的分类文章列表中
            processInsert(after);
            // 从旧的分类文章列表删除
            processDelete(before);
        }
    }

    @Override
    public void processDelete(Article deletedEntity) {
        String key = buildKey(deletedEntity.getCategoryId());
        RedisClient.zRem(key, deletedEntity.getId());
    }

    private String buildKey(Integer cateId) {
        return RedisConstants.CATE_ARTICLE_LIST + cateId;
    }
}
