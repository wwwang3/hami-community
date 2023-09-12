package top.wang3.hami.core.service.stat;

import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.ArticleStatDTO;
import top.wang3.hami.common.dto.UserStat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 计数业务接口
 * 文章数据
 * 用户数据
 * 评论数据 //要全量同步数据库数据到Redis
 * // 重构：使用CacheAside模式更新数据库的同时，删除缓存
 */
public interface CountService {

    ArticleStatDTO getArticleStatById(int articleId);

    UserStat getUserStatById(Integer userId);

    List<ArticleStatDTO> getArticleStatByIds(List<Integer> articleIds);

    List<UserStat> getUserStatByUserIds(List<Integer> userIds);

    static Map<String, Integer> setUserStatToMap(UserStat userStat) {
        if (userStat == null)
            userStat = new UserStat();
        HashMap<String, Integer> data = new HashMap<>();
        data.put(Constants.USER_TOTAL_FOLLOWINGS, userStat.getFollowings());
        data.put(Constants.USER_TOTAL_FOLLOWERS, userStat.getFollowers());
        data.put(Constants.USER_TOTAL_LIKES, userStat.getTotalLikes());
        data.put(Constants.USER_TOTAL_COMMENTS, userStat.getTotalComments());
        data.put(Constants.USER_TOTAL_COLLECTS, userStat.getTotalCollects());
        data.put(Constants.USER_TOTAL_ARTICLES, userStat.getTotalArticles());
        data.put(Constants.USER_TOTAL_VIEWS, userStat.getTotalViews());
        return data;
    }

    static UserStat readUserStatFromMap(Map<String, Integer> data, Integer userId) {
        if (data == null || data.isEmpty()) return null;
        UserStat stat1 = new UserStat();
        stat1.setUserId(userId);
        stat1.setFollowings(data.get(Constants.USER_TOTAL_FOLLOWINGS));
        stat1.setFollowers(data.get(Constants.USER_TOTAL_FOLLOWERS));
        stat1.setTotalLikes(data.get(Constants.USER_TOTAL_LIKES));
        stat1.setTotalComments(data.get(Constants.USER_TOTAL_COMMENTS));
        stat1.setTotalCollects(data.get(Constants.USER_TOTAL_COLLECTS));
        stat1.setTotalArticles(data.get(Constants.USER_TOTAL_ARTICLES));
        stat1.setTotalViews(data.get(Constants.USER_TOTAL_VIEWS));
        return stat1;
    }

}
