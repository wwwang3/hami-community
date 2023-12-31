package top.wang3.hami.core.service.stat;

import org.springframework.lang.NonNull;
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

    @NonNull
    Integer getUserArticleCount(Integer userId);

    @NonNull
    Integer getUserFollowingCount(Integer userId);

    @NonNull
    Integer getUserFollowerCount(Integer userId);

    Map<String, Integer> getUserDailyDataGrowing(Integer userId);

    Map<Integer, ArticleStatDTO> loadArticleStateCaches(List<Integer> ids);

    void loadUserStatCaches(List<Integer> userIds);

    UserStatDTO loadUserStatDTO(String key, Integer userId);

}
