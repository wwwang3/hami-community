package top.wang3.hami.core.service.article.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.canal.CanalEntryHandler;
import top.wang3.hami.common.canal.annotation.CanalListener;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.util.RandomUtils;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.article.ArticleService;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * 分类文章数量缓存同步
 * todo?: 将文章总数，分类文章总数用hash存储，感觉比较好一点，加载一次全部能加载出来
 */
@Component
@CanalListener(value = "article")
@RequiredArgsConstructor
@Slf4j
public class CateArticleCountHandler implements CanalEntryHandler<Article> {

    private final ArticleService articleService;

    @Override
    public void processInsert(Article entity) {
        Integer cateId = entity.getCategoryId();
        //插入, 文章数+1
        updateCount(cateId, 1);
    }

    @Override
    public void processUpdate(Article before, Article after) {
        if (CanalEntryHandler.isLogicDelete(before.getDeleted(), after.getDeleted())) {
            //删除
            processDelete(after);
        } else {
            if (!Objects.equals(before.getCategoryId(), after.getCategoryId())) {
                //分类Id更新
                processInsert(after);
                processDelete(before);
            }
        }
    }

    @Override
    public void processDelete(Article deletedEntity) {
        //删除, 文章数-1
        updateCount(deletedEntity.getCategoryId(), -1);
    }

    private void updateCount(Integer cateId, int delta) {
        String key = RedisConstants.CATE_ARTICLE_COUNT + cateId;
        if (RedisClient.expire(key, RandomUtils.randomLong(10, 100), TimeUnit.HOURS)) {
            //success
            RedisClient.incrBy(key, delta);
        } else {
            //没有或者过期
            articleService.getArticleCount(cateId);
        }
    }
}
