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
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.cache.CacheService;
import top.wang3.hami.core.service.article.repository.ArticleRepository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Component
@CanalRabbitHandler(value = "article", container = "canal-article-container-1")
@RequiredArgsConstructor
@Slf4j
public class ArticleCountHandler implements CanalEntryHandler<Article> {

    private final ArticleRepository articleRepository;
    private final CacheService cacheService;

    private RedisScript<Long> insert_script;
    private RedisScript<Long> update_script;

    @PostConstruct
    public void init() {
        insert_script = RedisClient.loadScript("/META-INF/scripts/article_count_insert.lua");
        update_script = RedisClient.loadScript("/META-INF/scripts/article_count_update.lua");
    }

    @Override
    public void processInsert(Article entity) {
        // 文章总数+1 分类文章数+1
        handleInsertOrDelete(entity, 1);
    }

    @Override
    public void processUpdate(Article before, Article after) {
        Byte old = before.getDeleted();
        Byte now = after.getDeleted();
        Integer oldCateId = before.getCategoryId();
        Integer nowCateId = after.getCategoryId();
        if (isLogicDelete(old, now)) {
            // 逻辑删除
            processDelete(after);
        } else if (!Objects.equals(oldCateId, nowCateId)) {
            // 修改了分类
            final String key = RedisConstants.ARTICLE_COUNT_KEY;
            final String oldCate = RedisConstants.CATE_ARTICLE_COUNT + oldCateId;
            final String nowCate = RedisConstants.CATE_ARTICLE_COUNT + nowCateId;
            Long result = RedisClient.executeScript(
                    update_script,
                    List.of(key, oldCate, nowCate),
                    List.of(TimeoutConstants.ARTICLE_COUNT_EXPIRE)
            );
            if (result == null || result == 0) {
                // 缓存过期, 重新设置缓存, 这里已经是mysql数据变化后
                cacheService.asyncSetHashCache(
                        key,
                        articleRepository.getArticleCount(),
                        TimeoutConstants.ARTICLE_COUNT_EXPIRE,
                        TimeUnit.MILLISECONDS
                );
            }
        }
    }

    @Override
    public void processDelete(Article deletedEntity) {
        // 文章总数-1 分类文章数-1
        handleInsertOrDelete(deletedEntity, -1);
    }

    private void handleInsertOrDelete(Article article, long delta) {
        final String key = RedisConstants.ARTICLE_COUNT_KEY;
        final String totalHKey = RedisConstants.TOTAL_ARTICLE_COUNT;
        final String cateHKey = RedisConstants.CATE_ARTICLE_COUNT + article.getCategoryId();
        Long result = RedisClient.executeScript(insert_script, List.of(key, totalHKey, cateHKey),
                List.of(TimeoutConstants.ARTICLE_COUNT_EXPIRE, delta));
        if (result == null || result == 0) {
            // 缓存过期, 重新设置缓存, 这里已经是mysql数据变化后
            cacheService.asyncSetHashCache(
                    key,
                    articleRepository.getArticleCount(),
                    TimeoutConstants.ARTICLE_COUNT_EXPIRE,
                    TimeUnit.MILLISECONDS
            );
        }
    }
}
