package top.wang3.hami.web.init;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.converter.ArticleConverter;
import top.wang3.hami.common.dto.article.ArticleDTO;
import top.wang3.hami.common.dto.article.ArticleInfo;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.model.ArticleDO;
import top.wang3.hami.common.model.User;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.job.RefreshStatTaskService;
import top.wang3.hami.core.service.article.impl.ArticleServiceImpl;
import top.wang3.hami.core.service.article.repository.ArticleRepository;
import top.wang3.hami.core.service.user.repository.UserRepository;

import java.util.List;
import java.util.Map;

//@Component
@RequiredArgsConstructor
@Order(4)
public class RedisCacheInitializer implements ApplicationRunner {

    private final ArticleRepository articleRepository;

    private final UserRepository userRepository;

    private final RefreshStatTaskService refreshStatTaskService;

    private final ArticleServiceImpl articleService;

    @CostLog
    @Override
    public void run(ApplicationArguments args) {
        cacheArticle();
        cacheUser();
        refreshStatTaskService.refreshArticleStat();
        refreshStatTaskService.refreshUserStat();
    }


    private void cacheArticle() {
        int lastId = 0;
        while (true) {
            List<Integer> ids = articleRepository.scanArticleIds(lastId, 1000);
            if (ids == null || ids.isEmpty()) {
                break;
            }
            List<ArticleDO> articles = articleRepository.scanArticles(ids);
            cacheArticleInfo(articles);
            cacheArticleContent(articles);
            lastId = ids.get(ids.size() - 1);
        }
    }

    private void cacheUser() {
        int lastId = 0;
        while (true) {
            List<Integer> userIds = userRepository.scanUserIds(lastId, 2000);
            if (userIds == null || userIds.isEmpty()) {
                break;
            }
            List<User> users = userRepository.getUserByIds(userIds);
            Map<String, User> map = ListMapperHandler.listToMap(users,
                    user -> Constants.USER_INFO + user.getUserId());
            RedisClient.cacheMultiObject(map);
            lastId = userIds.get(users.size() - 1);
        }
    }

    private void cacheArticleInfo(List<ArticleDO> articles) {
        List<ArticleInfo> articleInfos = ArticleConverter.INSTANCE.toArticleInfos(articles);
        List<ArticleDTO> dtos = ArticleConverter.INSTANCE.toArticleDTOS(articleInfos);
        articleService.buildCategory(dtos);
        articleService.buildArticleTags(dtos);
        Map<String, ArticleDTO> map = ListMapperHandler.listToMap(dtos,
                item -> Constants.ARTICLE_INFO + item.getId());
        RedisClient.cacheMultiObject(map);
    }

    private void cacheArticleContent(List<ArticleDO> articleDOS) {
        Map<String, String> map = ListMapperHandler.listToMap(articleDOS,
                item -> Constants.ARTICLE_CONTENT + item.getId(), Article::getContent);
        RedisClient.cacheMultiObject(map);
    }
}
