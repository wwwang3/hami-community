package top.wang3.hami.core.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.model.Category;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.article.repository.CategoryRepository;

import java.util.List;

import static top.wang3.hami.core.init.InitializerEnums.ARTICLE_LIST_CACHE;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class ArticleListInitializer implements HamiInitializer {

    private final CategoryRepository categoryRepository;
    private final ArticleService articleService;

    @Override
    public InitializerEnums getName() {
        return ARTICLE_LIST_CACHE;
    }

    @Override
    public boolean alwaysExecute() {
        return false;
    }

    @Override
    public void run() {
        cacheTotal();
        cacheSub();
    }

    private void cacheTotal() {
        articleService.loadArticleListCache(RedisConstants.ARTICLE_LIST, null, -1, -1);
    }

    private void cacheSub() {
        List<Category> categories = categoryRepository.getAllCategories();
        for (Category category : categories) {
            Integer cateId = category.getId();
            articleService.loadArticleListCache(RedisConstants.CATE_ARTICLE_LIST + cateId, cateId, -1, -1);
        }
    }

}
