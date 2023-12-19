package top.wang3.hami.core.service.article.handler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import top.wang3.hami.canal.CanalEntryHandler;
import top.wang3.hami.canal.annotation.CanalRabbitHandler;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.article.ArticleService;


@Component
@CanalRabbitHandler(value = "article", container = "canal-article-container-1")
@RequiredArgsConstructor
@Slf4j
public class UserArticleCountHandler implements CanalEntryHandler<Article> {

    private final ArticleService articleService;

    private RedisScript<Long> insert_script;

    @PostConstruct
    public void init() {
        insert_script = RedisClient.loadScript("/META-INF/scripts/article_count_insert.lua");
    }

    @Override
    public void processInsert(Article entity) {
        final String key = RedisConstants.USER_ARTICLE_COUNT + entity.getUserId();
    }

    @Override
    public void processUpdate(Article before, Article after) {

    }

    @Override
    public void processDelete(Article deletedEntity) {

    }
}
