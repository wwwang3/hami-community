package top.wang3.hami.core.handler;


import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.annotation.CanalListener;
import top.wang3.hami.common.canal.CanalEntryHandler;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.UserStat;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.util.RedisClient;

@Component
@Order(value = 2) //越大优先级越低
@CanalListener("article_stat")
@Slf4j
public class UserStatCanalHandler implements CanalEntryHandler<ArticleStat> {

    @Override
    public void processInsert(ArticleStat entity) {
        String key = getKey(entity.getUserId());
        //增加一条文章数据记录，表示新增了一篇文章
        RedisClient.hIncr(key, "articles", 1);
        log.debug("insert success");
    }

    @Override
    public void processUpdate(ArticleStat before, ArticleStat after) {
        UserStat stat = calculate(before, after);
        String key = getKey(after.getUserId());
        if (stat.getTotalViews() != 0) {
            RedisClient.hIncr(key, Constants.USER_TOTAL_VIEWS, stat.getTotalViews());
        }
        if (stat.getTotalLikes() != 0) {
            RedisClient.hIncr(key, Constants.USER_TOTAL_LIKES, stat.getTotalLikes());
        }
        if (stat.getTotalComments() != 0) {
            RedisClient.hIncr(key, Constants.USER_TOTAL_COMMENTS, stat.getTotalComments());
        }
        if (stat.getTotalCollects()!= 0) {
            RedisClient.hIncr(key, Constants.USER_TOTAL_COLLECTS, stat.getTotalCollects());
        }
        log.debug("user-stat-handler update to Redis success");
    }

    @Override
    public void processDelete(ArticleStat deletedEntity) {
        String key = getKey(deletedEntity.getUserId());
        RedisClient.deleteObject(key);
    }

    private UserStat calculate(ArticleStat before, ArticleStat after) {
        UserStat stat = new UserStat();
        stat.setTotalLikes(after.getLikes() - before.getLikes());
        stat.setTotalComments(after.getComments() - before.getComments());
        stat.setTotalCollects(after.getCollects() - before.getCollects());
        stat.setTotalViews(after.getViews() - before.getViews());
        return stat;
    }

    private String getKey(Integer userId) {
        return Constants.COUNT_TYPE_USER + userId;
    }

}
