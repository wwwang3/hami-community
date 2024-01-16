package top.wang3.hami.core.init;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.model.UserStat;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.stat.repository.ArticleStatRepository;
import top.wang3.hami.core.service.stat.repository.UserStatRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Component
@Order(6)
@RequiredArgsConstructor
public class StatInitializer implements HamiInitializer {

    private final UserStatRepository userStatRepository;
    private final ArticleStatRepository articleStatRepository;

    @Override
    public InitializerEnums getName() {
        return InitializerEnums.STAT_CACHE;
    }

    @Override
    public void run() {
        cacheUserStat();
        cacheArticleStat();
    }

    public void cacheArticleStat() {
        Page<ArticleStat> page = new Page<>(1, 1000);
        int size = 100;
        int i = 1;
        while (i <= size) {
            List<ArticleStat> stats = articleStatRepository.scanArticle(page);
            Map<String, ArticleStat> map = ListMapperHandler.listToMap(stats,
                    stat -> RedisConstants.STAT_TYPE_ARTICLE + stat.getArticleId());
            RedisClient.cacheMultiObject(map, TimeoutConstants.ARTICLE_STAT_EXPIRE, TimeUnit.MILLISECONDS);
            ++i;
            page.setCurrent(i);
            page.setRecords(null);
            // 后续不要查总数了
            page.setSearchCount(false);
            if (stats.isEmpty() || !page.hasNext()) {
                break;
            }
        }
    }

    public void cacheUserStat() {
        Page<UserStat> page = new Page<>(1, 1000);
        int size = 100;
        int i = 1;
        while (i <= size) {
            List<UserStat> stats = userStatRepository.scanUserStat(page);
            Map<String, UserStat> map = ListMapperHandler.listToMap(stats,
                    stat -> RedisConstants.STAT_TYPE_USER + stat.getUserId());
            RedisClient.cacheMultiObject(map, TimeoutConstants.USER_STAT_EXPIRE, TimeUnit.MILLISECONDS);
            ++i;
            page.setCurrent(i);
            page.setRecords(null);
            page.setSearchCount(false);
            if (stats.isEmpty() || !page.hasNext()) {
                break;
            }
        }
    }
}
