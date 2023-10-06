package top.wang3.hami.core.service.stat;

import top.wang3.hami.common.dto.article.ArticleStatDTO;
import top.wang3.hami.common.dto.user.UserStat;

import java.util.List;
import java.util.Map;

/**
 * 计数业务接口
 * 文章数据
 * 用户数据
 */
public interface CountService {

    ArticleStatDTO getArticleStatById(int articleId);

    UserStat getUserStatById(Integer userId);

    Map<Integer, ArticleStatDTO> getArticleStatByIds(List<Integer> articleIds);

    Map<Integer, UserStat> getUserStatByUserIds(List<Integer> userIds);

    Map<String, Integer> getUserDailyDataGrowing(Integer userId);

}
