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

    Map<Integer, UserStatDTO> getUserStatByIds(List<Integer> userIds);

    @NonNull
    Integer getUserArticleCount(Integer userId);

    @NonNull
    @SuppressWarnings("unused")
    Integer getUserFollowingCount(Integer userId);

    @NonNull
    @SuppressWarnings("unused")
    Integer getUserFollowerCount(Integer userId);

    Map<String, Integer> getUserYesterdayDataGrowing(Integer userId);


}
