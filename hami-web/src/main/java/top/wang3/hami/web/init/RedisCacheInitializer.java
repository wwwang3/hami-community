package top.wang3.hami.web.init;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.builder.ArticleOptionsBuilder;
import top.wang3.hami.common.dto.builder.UserOptionsBuilder;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.model.ArticleDO;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.article.repository.ArticleRepository;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.core.service.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//@Component
@RequiredArgsConstructor
@Order(4)
public class RedisCacheInitializer implements ApplicationRunner {

    private final ArticleRepository articleRepository;

    private final UserRepository userRepository;

    private final ArticleService articleService;

    private final UserService userService;

    @CostLog
    @Override
    public void run(ApplicationArguments args) {
//        cacheArticle();
//        cacheUser();
        cacheArticleContent();
    }

    private void cacheArticle() {
        int lastId = 0;
        while (true) {
            List<Integer> ids = articleRepository.scanArticleIds(lastId, 1000);
            if (ids == null || ids.isEmpty()) {
                break;
            }
            articleService.listArticleById(ids, new ArticleOptionsBuilder());
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
            userService.listAuthorInfoById(userIds, new UserOptionsBuilder());
            lastId = userIds.get(userIds.size() - 1);
        }
    }

    private void cacheArticleContent() {
        int lastId = 0;
        while (true) {
            List<Integer> ids = articleRepository.scanArticleIds(lastId, 1000);
            if (ids == null || ids.isEmpty()) {
                break;
            }
            List<ArticleDO> dos = articleRepository.scanArticles(ids);
            cacheArticleContent(dos);
            lastId = ids.get(ids.size() - 1);
        }
    }


    private void cacheArticleContent(List<ArticleDO> articleDOS) {
        Map<String, String> map = ListMapperHandler.listToMap(articleDOS,
                item -> Constants.ARTICLE_CONTENT + item.getId(), Article::getContent);
        RedisClient.cacheMultiObject(map, 10, 30, TimeUnit.DAYS);
    }
}
