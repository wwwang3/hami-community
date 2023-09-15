package top.wang3.hami.core.service.article.handler;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.annotation.CanalListener;
import top.wang3.hami.common.canal.CanalEntryHandler;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.util.RedisClient;

import java.util.Objects;

@Component
@CanalListener(value = "article")
@Slf4j
public class ArticleCanalHandler implements CanalEntryHandler<Article> {

    private RedisScript<Long> insert_article_script;
    private RedisScript<Long> update_article_script;
    private RedisScript<Long> delete_article_script;

    @PostConstruct
    public void init() {
        insert_article_script = RedisClient.loadScript("/META-INF/scripts/insert_article.lua");
        update_article_script = RedisClient.loadScript("/META-INF/scripts/update_article.lua");
        delete_article_script = RedisClient.loadScript("/META-INF/scripts/delete_article.lua");
    }

    @Override
    public void processInsert(Article entity) {
        log.info("start to process insert article to redis");
        Integer id = entity.getId();
        Integer userId = entity.getUserId();
        Integer cateId = entity.getCategoryId();
        long time = entity.getCtime().getTime();
        Long res = RedisClient.excuteScript(insert_article_script, null,  id, userId, cateId, time);
        log.debug("process finish, res: {}", res);
    }

    @Override
    public void processUpdate(Article before, Article after) {
        log.info("start to process update article to redis");
        if (isLogicDelete(before, after)) {
            processDelete(after);
        }
        Integer id = after.getId();
        Integer oldCate = before.getCategoryId();
        Integer newCate = after.getCategoryId();
        long time = after.getCtime().getTime();
        Long res = null;
        if (!Objects.equals(oldCate, newCate)) {
            res = RedisClient.excuteScript(update_article_script, null, id, oldCate, newCate, time);
        }
        log.debug("process finish, res: {}", res);
    }

    @Override
    public void processDelete(Article deletedEntity) {
        log.info("start to process update article to redis");
        Integer id = deletedEntity.getId();
        Integer categoryId = deletedEntity.getCategoryId();
        Integer userId = deletedEntity.getUserId();
        Long res = RedisClient.excuteScript(delete_article_script, null, id, userId, categoryId);
        log.debug("process finish, res: {}", res);
    }



    private boolean isLogicDelete(Article before, Article after) {
        return before.getDeleted() == 0 && after.getDeleted() == 1;
    }
}
