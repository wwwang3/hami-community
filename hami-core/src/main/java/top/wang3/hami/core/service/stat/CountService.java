package top.wang3.hami.core.service.stat;

import top.wang3.hami.common.dto.stat.ArticleStatDTO;
import top.wang3.hami.common.dto.stat.UserStatDTO;

import java.util.List;
import java.util.Map;

/**
 * 计数业务接口
 * 文章数据
 * 用户数据
 */
public interface CountService {

    ArticleStatDTO getArticleStatById(int articleId);

    UserStatDTO getUserStatDTOById(Integer userId);

    Map<Integer, ArticleStatDTO> getArticleStatByIds(List<Integer> articleIds);

    Map<Integer, UserStatDTO> getUserStatDTOByUserIds(List<Integer> userIds);

    Map<String, Integer> getUserDailyDataGrowing(Integer userId);

    Map<Integer, ArticleStatDTO> loadArticleStateCaches(List<Integer> ids);

    UserStatDTO loadUserStatDTO(String key, Integer userId);
}
