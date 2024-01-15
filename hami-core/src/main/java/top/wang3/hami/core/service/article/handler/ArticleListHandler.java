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


@Component
@CanalRabbitHandler(value = "article", container = "canal-article-container-2")
@RequiredArgsConstructor
@Slf4j
public class ArticleListHandler implements CanalEntryHandler<Article> {

    private final ArticleCacheService articleCacheService;
    private final LockTemplate lockTemplate;

    private RedisScript<Long> insert_article_script;

    @PostConstruct
    public void init() {
        insert_article_script = RedisClient.loadScript("/META-INF/scripts/insert_article_list.lua");
    }

    @Override
    public void processInsert(Article entity) {
        Integer id = entity.getId();
        long ctime = entity.getCtime().getTime();
        final String key = RedisConstants.ARTICLE_LIST;
        long timeout = TimeoutConstants.ARTICLE_LIST_EXPIRE;
        Long result = RedisClient.executeScript(
                insert_article_script,
                List.of(key),
                List.of(id, ctime, timeout, ZPageHandler.DEFAULT_MAX_SIZE)
        );
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
        }
    }

    @Override
    public void processDelete(Article deletedEntity) {
        // 缓存过期重新读取吧
        Integer id = deletedEntity.getId();
        RedisClient.zRem(RedisConstants.ARTICLE_LIST, id);
    }
}
