package top.wang3.hami.core.service.article.handler;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.canal.CanalEntryHandler;
import top.wang3.hami.common.canal.annotation.CanalListener;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.util.RedisClient;

import java.util.List;
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
        log.info("entity: {} ctime: {}", entity, entity.getCtime().getTime());
        Integer id = entity.getId();
        Integer userId = entity.getUserId();
        Integer cateId = entity.getCategoryId();
        long time = entity.getCtime().getTime();
        List<String> keys = buildKeys(userId, cateId);
        Long res = RedisClient.executeScript(insert_article_script, keys, List.of(id, time));
    }

    @Override
    public void processUpdate(Article before, Article after) {
        if (isLogicDelete(before, after)) {
            processDelete(after);
        } else {
            Integer id = after.getId();
            Integer oldCate = before.getCategoryId();
            Integer newCate = after.getCategoryId();
            long time = after.getCtime().getTime();
            Long res = null;
            if (!Objects.equals(oldCate, newCate)) {
                String oldCateListKey = RedisConstants.CATE_ARTICLE_LIST + oldCate;
                String newOldCateListKey = RedisConstants.CATE_ARTICLE_LIST + newCate;
                List<String> keys = List.of(oldCateListKey, newOldCateListKey);
                List<?> args = List.of(id, time);
                res = RedisClient.executeScript(update_article_script, keys, args);
            }
        }
    }

    @Override
    public void processDelete(Article deletedEntity) {
        Integer id = deletedEntity.getId();
        Integer categoryId = deletedEntity.getCategoryId();
        Integer userId = deletedEntity.getUserId();
        List<String> keys = buildKeys(categoryId, userId);
        Long res = RedisClient.executeScript(delete_article_script, keys, List.of(id));
    }

    private boolean isLogicDelete(Article before, Article after) {
        return before.getDeleted() == 0 && after.getDeleted() == 1;
    }

    private List<String> buildKeys(Integer cateId, Integer userId) {
        String articleListKey = RedisConstants.ARTICLE_LIST;
        String cateListKey = RedisConstants.CATE_ARTICLE_LIST + cateId;
        String userArticleListKey = RedisConstants.LIST_USER_ARTICLE + userId;
        return List.of(articleListKey, cateListKey, userArticleListKey);
    }
}
