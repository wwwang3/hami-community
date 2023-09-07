package top.wang3.hami.core.service.common.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.ArticleStatDTO;
import top.wang3.hami.common.dto.UserStat;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.article.ArticleStatService;
import top.wang3.hami.core.service.common.CountService;
import top.wang3.hami.core.service.user.UserFollowService;

/**
 * todo 优化
 */
@Service
@RequiredArgsConstructor
public class CountServiceImpl implements CountService {

    private final ArticleStatService articleStatService;

    private final UserFollowService userFollowService;

    @Override
    public ArticleStatDTO getArticleStatById(int articleId) {
        String redisKey = Constants.COUNT_TYPE_ARTICLE + articleId;
        ArticleStatDTO dto = RedisClient.getCacheObject(redisKey);
        if (dto != null) return dto;
        synchronized (this) {
            dto = RedisClient.getCacheObject(redisKey);
            if (dto != null) return dto;
            ArticleStatDTO stat = articleStatService.getArticleStatByArticleId(articleId);
            //回写
            //todo 设置缓存时间
            RedisClient.setCacheObject(redisKey, stat);
            return stat;
        }
    }

    @Override
    public UserStat getUserStatById(Integer userId) {
        String redisKey = Constants.COUNT_TYPE_USER + userId;
        UserStat stat = RedisClient.getCacheObject(redisKey);
        if (stat != null) return stat;
        //并发高就g了
        synchronized (this) {
            stat = RedisClient.getCacheObject(redisKey);
            if (stat != null) return stat;
            stat = articleStatService.getUserStatistics(userId);
            if (stat == null) {
                stat = new UserStat();
            }
            Long followerCount = userFollowService.getUserFollowerCount(userId);
            Long followingCount = userFollowService.getUserFollowingCount(userId);
            stat.setFollowers(followerCount);
            stat.setFollowings(followingCount);
            RedisClient.setCacheObject(redisKey, stat);
            return stat;
        }
    }
}
