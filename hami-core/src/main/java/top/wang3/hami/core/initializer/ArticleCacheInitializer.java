package top.wang3.hami.core.initializer;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.builder.ArticleOptionsBuilder;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.model.ArticleDO;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.article.repository.ArticleRepository;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.core.service.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Order(3)
@RequiredArgsConstructor
public class ArticleCacheInitializer implements HamiInitializer {


    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final ArticleService articleService;
    private final UserService userService;

    @Override
    public String getName() {
        return ARTICLE_CACHE;
    }

    @Override
    public void run() {
        cacheArticle();
        cacheArticleContent();
    }

    private void cacheArticle() {
        int lastId = 0;
        while (true) {
            List<Integer> ids = articleRepository.scanArticleIds(lastId, 1000);
            if (ids == null || ids.isEmpty()) {
                break;
            }
            articleService.listArticleById(ids, new ArticleOptionsBuilder()
                    .noAuthor()
                    .noInteract());
            lastId = ids.get(ids.size() - 1);
        }
    }

    private void cacheArticleContent() {
        int lastId = 0;
        while (true) {
            List<Integer> ids = articleRepository.scanArticleIds(lastId, 500);
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
