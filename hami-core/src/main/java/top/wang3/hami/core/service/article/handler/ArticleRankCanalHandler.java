package top.wang3.hami.core.service.article.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wang3.hami.canal.CanalEntryHandler;
import top.wang3.hami.canal.annotation.CanalRabbitHandler;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.util.RedisClient;

@Component
@CanalRabbitHandler(value = "article", container = "canal-article-container-2")
@RequiredArgsConstructor
@Slf4j
public class ArticleRankCanalHandler implements CanalEntryHandler<Article> {

    @Override
    public void processInsert(Article entity) {
        //ignore it
    }

    @Override
    public void processUpdate(Article before, Article after) {
        //ignore it
        if (isLogicDelete(before.getDeleted(), after.getDeleted())) {
            processDelete(after);
        }
    }

    @Override
    public void processDelete(Article deletedEntity) {
        Integer id = deletedEntity.getId();
        String total_key = RedisConstants.OVERALL_HOT_ARTICLE;
        String cate_key = RedisConstants.CATE_HOT_ARTICLE + deletedEntity.getCategoryId();
        Long res1 = RedisClient.zRem(total_key, id);
        Long res2 = RedisClient.zRem(cate_key, id);
        log.info("article deleted, remove it from hot_article_list, res1: {}, res2: {}", res1, res2);
    }
}
