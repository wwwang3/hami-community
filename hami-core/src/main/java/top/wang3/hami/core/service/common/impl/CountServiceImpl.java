package top.wang3.hami.core.service.common.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.ArticleStatDTO;
import top.wang3.hami.common.dto.UserStat;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.article.ArticleStatService;
import top.wang3.hami.core.service.common.CountService;
import top.wang3.hami.core.service.user.UserFollowService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * todo 优化
 */
@Service
@RequiredArgsConstructor
public class CountServiceImpl implements CountService {

    private final ArticleStatService articleStatService;

    private final UserFollowService userFollowService;

    private final Lock lock = new ReentrantLock();

    @Override
    public ArticleStatDTO getArticleStatById(int articleId) {
        String redisKey = Constants.COUNT_TYPE_ARTICLE + articleId;
        ArticleStatDTO stat = RedisClient.getCacheObject(redisKey);
        return stat == null ? new ArticleStatDTO() : stat;
//        Map<String, Integer> data = RedisClient.getCacheMap(redisKey);
//        return readArticleStatFromMap(data);
    }

    @Override
    public UserStat getUserStatById(Integer userId) {
        String redisKey = Constants.COUNT_TYPE_USER + userId;
        Map<String, Integer> data = RedisClient.hMGetAll(redisKey);
        UserStat stat = readUserStatFromMap(data);
        if (stat != null) return stat;
        //不回表查询了，完全将Redis当数据库用(我是Σ(☉▽☉"a)
        //通过Canal监听Binlog同步二者数据 (通过hIncr)
        //没有说明没有人点赞，评论，关注
        return new UserStat();
    }

    @Transactional
    @Override
    public void increaseViews(Collection<ArticleStat> stats) {
        articleStatService.updateBatchById(stats);
    }

    private ArticleStatDTO readArticleStatFromMap(Map<String, Integer> data) {
        if (data == null || data.isEmpty()) return new ArticleStatDTO();
        ArticleStatDTO dto = new ArticleStatDTO();
        dto.setViews(data.get(Constants.ARTICLE_VIEWS));
        dto.setLikes(data.get(Constants.ARTICLE_LIKES));
        dto.setComments(data.get(Constants.ARTICLE_COMMENTS));
        dto.setCollects(data.get(Constants.ARTICLE_COLLECTS));
        return dto;
    }


    private UserStat readUserStatFromMap(Map<String, Integer> data) {
        if (data == null || data.isEmpty()) return null;
        UserStat stat1 = new UserStat();
        stat1.setFollowings(data.get(Constants.USER_TOTAL_FOLLOWINGS));
        stat1.setFollowers(data.get(Constants.USER_TOTAL_FOLLOWERS));
        stat1.setTotalLikes(data.get(Constants.USER_TOTAL_LIKES));
        stat1.setTotalComments(data.get(Constants.USER_TOTAL_COMMENTS));
        stat1.setTotalCollects(data.get(Constants.USER_TOTAL_COLLECTS));
        stat1.setArticles(data.get(Constants.USER_TOTAL_ARTICLES));
        stat1.setTotalViews(data.get(Constants.USER_TOTAL_VIEWS));
        return stat1;
    }

    private Map<String, Integer> setUserStatToMap(UserStat userStat) {
        if (userStat == null)
            userStat = new UserStat();
        HashMap<String, Integer> data = new HashMap<>();
        data.put(Constants.USER_TOTAL_FOLLOWINGS, userStat.getFollowings());
        data.put(Constants.USER_TOTAL_FOLLOWERS, userStat.getFollowers());
        data.put(Constants.USER_TOTAL_LIKES, userStat.getTotalLikes());
        data.put(Constants.USER_TOTAL_COMMENTS, userStat.getTotalComments());
        data.put(Constants.USER_TOTAL_COLLECTS, userStat.getTotalCollects());
        data.put(Constants.USER_TOTAL_ARTICLES, userStat.getArticles());
        data.put(Constants.USER_TOTAL_VIEWS, userStat.getTotalViews());
        return data;
    }

    private Map<String, Integer> setArticleStaToMap(ArticleStatDTO dto) {
        Map<String, Integer> map = new HashMap<>();
        map.put(Constants.ARTICLE_VIEWS, dto.getViews());
        map.put(Constants.ARTICLE_LIKES, dto.getLikes());
        map.put(Constants.ARTICLE_COMMENTS, dto.getComments());
        map.put(Constants.ARTICLE_COLLECTS, dto.getCollects());
        return map;
    }
}
