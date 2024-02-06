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
import top.wang3.hami.common.util.ZPageHandler;

import java.util.List;

@Component
@CanalRabbitHandler(value = "article", container = "canal-article-container-2")
@RequiredArgsConstructor
@Slf4j
public class UserArticleListHandler implements CanalEntryHandler<Article> {



    private RedisScript<Long> insert_article_script;

    @PostConstruct
    public void init() {
        insert_article_script = RedisClient.loadScript("/META-INF/scripts/insert_article_list.lua");
    }

    @Override
    public void processInsert(Article entity) {
        Integer id = entity.getId();
        Integer userId = entity.getUserId();
        String key = RedisConstants.USER_ARTICLE_LIST + userId;
        long timeout = TimeoutConstants.USER_ARTICLE_LIST_EXPIRE;
        long ctime = entity.getCtime().getTime();
        log.info("article inserted, add to redis-user-article-list, article: {}", entity);
        RedisClient.executeScript(
                insert_article_script,
                List.of(key),
                List.of(id, ctime, timeout, ZPageHandler.DEFAULT_MAX_SIZE)
        );
    }

    @Override
    public void processUpdate(Article before, Article after) {
        if (isLogicDelete(before.getDeleted(), after.getDeleted())) {
            processDelete(after);
        }
    }

    @Override
    public void processDelete(Article deletedEntity) {
        String key = RedisConstants.USER_ARTICLE_LIST + deletedEntity.getUserId();
        RedisClient.zRem(key, deletedEntity.getId());
    }
}
