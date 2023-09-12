package top.wang3.hami.core.service.stat.handler;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import top.wang3.hami.common.annotation.CanalListener;
import top.wang3.hami.common.canal.CanalEntryHandler;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.UserStat;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.util.RedisClient;

import java.util.List;


@CanalListener("article_stat")
@Slf4j
public class UserStatCanalHandler implements CanalEntryHandler<ArticleStat> {

    private RedisScript<Long> redisScript;

    @PostConstruct
    public void loadFollowScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        String path = "/META-INF/scripts/update_user_stat.lua";
        ResourceScriptSource source = new ResourceScriptSource(new ClassPathResource(path));
        script.setScriptSource(source);
        script.setResultType(Long.class);
        redisScript = script;
        log.debug("success load lua script: {}", path);
    }

    @Override
    public void processInsert(ArticleStat entity) {
        String key = getKey(entity.getUserId());
        //增加一条文章数据记录，表示新增了一篇文章
        RedisClient.hIncr(key, Constants.USER_TOTAL_ARTICLES, 1);
        log.debug("insert success");
    }

    @Override
    public void processUpdate(ArticleStat before, ArticleStat after) {
        UserStat stat = calculate(before, after);
        String key = getKey(after.getUserId());
        RedisClient.excuteScript(redisScript, List.of(key), stat.getTotalViews(), stat.getTotalLikes(),
                stat.getTotalComments(), stat.getTotalCollects());
        log.debug("user-stat-handler update to Redis success");
    }

    @Override
    public void processDelete(ArticleStat deletedEntity) {
        String key = getKey(deletedEntity.getUserId());
        //删除
        RedisClient.excuteScript(redisScript, List.of(key), -deletedEntity.getViews(), - deletedEntity.getLikes(),
                deletedEntity.getComments(), deletedEntity.getCollects());
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
