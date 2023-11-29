package top.wang3.hami.core.service.article.handler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ArticleListHandler implements CanalEntryHandler<Article> {

    private final ArticleService articleService;

    private RedisScript<Long> insert_article_script;

    @PostConstruct
    public void init() {
        insert_article_script = RedisClient.loadScript("/META-INF/scripts/insert_article_list.lua");
    }

    @Override
    public void processInsert(Article entity) {
        log.info("entity: {} ctime: {}", entity, entity.getCtime().getTime());
        Integer id = entity.getId();
        long ctime = entity.getCtime().getTime();
        String key = RedisConstants.ARTICLE_LIST;
        if (RedisClient.expire(key, RandomUtils.randomLong(10, 100), TimeUnit.DAYS)) {
            //成功
            RedisClient.executeScript(insert_article_script, List.of(key), List.of(id, ctime));
        } else {
            //加载缓存
            articleService.loadArticleListCache(key, null, -1, -1);
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
        Integer id = deletedEntity.getId();
        RedisClient.zRem(RedisConstants.ARTICLE_LIST, id);
    }
}
