package top.wang3.hami.core.service.article.handler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.canal.CanalEntryHandler;
import top.wang3.hami.common.canal.annotation.CanalListener;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.util.RandomUtils;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.article.ArticleService;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@CanalListener(value = "article")
@RequiredArgsConstructor
public class UserArticleListHandler implements CanalEntryHandler<Article> {


    private final ArticleService articleService;

    private RedisScript<Long> insert_article_script;

    @PostConstruct
    public void init() {
        insert_article_script = RedisClient.loadScript("/META-INF/scripts/insert_article_list.lua");
    }

    @Override
    public void processInsert(Article entity) {
        Integer userId = entity.getUserId();
        String key = RedisConstants.LIST_USER_ARTICLE + userId;
        if (RedisClient.expire(key, RandomUtils.randomLong(10, 100), TimeUnit.HOURS)) {
            RedisClient.executeScript(insert_article_script,
                    List.of(key),
                    List.of(entity.getId(), entity.getCtime().getTime())
            );
        } else {
            articleService.loadUserArticleListCache(key, userId, -1, -1);
        }
    }

    @Override
    public void processUpdate(Article before, Article after) {
        if (CanalEntryHandler.isLogicDelete(before.getDeleted(), after.getDeleted())) {
            processDelete(after);
        }
    }

    @Override
    public void processDelete(Article deletedEntity) {
        String key = RedisConstants.LIST_USER_ARTICLE + deletedEntity.getUserId();
        RedisClient.zRem(key, deletedEntity.getId());
    }
}
