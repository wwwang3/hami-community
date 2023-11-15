package top.wang3.hami.core.init;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.core.service.article.repository.ArticleRepository;
import top.wang3.hami.core.service.stat.CountService;
import top.wang3.hami.core.service.user.repository.UserRepository;

import java.util.List;


@Component
@Order(6)
@RequiredArgsConstructor
public class StatInitializer implements HamiInitializer {

    private final CountService countService;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;

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
        int batchSize = 1000;
        int lastArticleId = 0;
        while (true) {
            List<Integer> ids = articleRepository.scanArticleIds(lastArticleId, batchSize);
            if (!CollectionUtils.isEmpty(ids)) {
                countService.loadArticleStateCaches(ids);
                lastArticleId = ids.get(ids.size() - 1);
            } else {
                break;
            }
        }
    }

    public void cacheUserStat() {
        int batchSize = 1000;
        int lastUserId = 0;
        while (true) {
            List<Integer> userIds = userRepository.scanUserIds(lastUserId, batchSize);
            if (!CollectionUtils.isEmpty(userIds)) {
                countService.loadUserStatCaches(userIds);
                lastUserId = userIds.get(userIds.size() - 1);
            } else {
                break;
            }
        }
    }
}
