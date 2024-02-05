package top.wang3.hami.core.service.stat.handler;

import org.springframework.stereotype.Component;
import top.wang3.hami.canal.CanalEntryHandler;
import top.wang3.hami.canal.annotation.CanalRabbitHandler;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.model.UserStat;
import top.wang3.hami.core.service.stat.repository.UserStatRepository;


@Component
@CanalRabbitHandler(value = "article_stat", container = "canal-stat-container-1")
public class SyncUserStatCanalHandler implements CanalEntryHandler<ArticleStat> {

    private final UserStatRepository userStatRepository;

    public SyncUserStatCanalHandler(UserStatRepository userStatRepository) {
        this.userStatRepository = userStatRepository;
    }

    @Override
    public void processInsert(ArticleStat entity) {

    }

    @Override
    public void processUpdate(ArticleStat before, ArticleStat after) {
        if (isLogicDelete(before.getDeleted(), after.getDeleted())) {
            // 删除
            processDelete(after);
        }
    }

    @Override
    public void processDelete(ArticleStat deletedEntity) {
        // 文章数据被删除, 更新用户数据
        // 感觉不更新也问题不大
        UserStat stat = new UserStat();
        stat.setUserId(deletedEntity.getUserId());
        stat.setTotalLikes(deletedEntity.getLikes() * -1);
        stat.setTotalComments(-deletedEntity.getComments() * -1);
        stat.setTotalCollects(-deletedEntity.getCollects() * -1);
        stat.setTotalViews(deletedEntity.getViews() * -1);
        stat.setTotalArticles(-1);
        userStatRepository.updateUserStat(stat);
    }
}
