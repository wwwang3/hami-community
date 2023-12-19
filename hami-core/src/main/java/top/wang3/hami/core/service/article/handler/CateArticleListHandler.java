package top.wang3.hami.core.service.article.handler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import top.wang3.hami.canal.CanalEntryHandler;
import top.wang3.hami.canal.annotation.CanalRabbitHandler;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.common.util.ZPageHandler;
import top.wang3.hami.core.service.article.ArticleService;

import java.util.List;
import java.util.Objects;


@Component
@CanalRabbitHandler(value = "article", container = "canal-article-container-2")
@RequiredArgsConstructor
public class CateArticleListHandler implements CanalEntryHandler<Article> {

    private final ArticleService articleService;

    private RedisScript<Long> insert_article_script;
    private RedisScript<Long> update_article_script;

    @PostConstruct
    public void init() {
        insert_article_script = RedisClient.loadScript("/META-INF/scripts/insert_article_list.lua");
        update_article_script = RedisClient.loadScript("/META-INF/scripts/update_cate_article_list.lua");
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
        if (result == null || result == 0) {
            articleService.loadArticleListCache(key, cateId, -1, -1);
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
            String oldCateKey = buildKey(before.getCategoryId());
            String newCateKey = buildKey(after.getCategoryId());
            RedisClient.executeScript(
                    update_article_script,
                    List.of(oldCateKey, newCateKey),
                    List.of(after.getId(), after.getCtime().getTime())
            );
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
