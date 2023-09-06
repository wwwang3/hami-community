package top.wang3.hami.core.service.common.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.ArticleStatDTO;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.article.ArticleStatService;
import top.wang3.hami.core.service.common.CountService;

@Service
@RequiredArgsConstructor
public class CountServiceImpl implements CountService {

    private  final ArticleStatService articleStatService;

    @Override
    public ArticleStatDTO getArticleStatById(int articleId) {
        String redisKey = Constants.COUNT_ARTICLE_STAT_V2 + articleId;
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
}
