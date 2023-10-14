package top.wang3.hami.core.initializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.dto.request.ArticlePageParam;
import top.wang3.hami.common.model.Category;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.article.repository.CategoryRepository;

import java.util.List;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class ArticleListInitializer implements HamiInitializer {

    private final CategoryRepository categoryRepository;
    private final ArticleService articleService;

    @Override
    public String getName() {
        return ARTICLE_LIST_CACHE;
    }

    @Override
    public void run() {
        cacheTotal();
        cacheSub();
    }

    private void cacheTotal() {
        ArticlePageParam page = new ArticlePageParam(null, null);
        page.setPageNum(1);
        page.setPageSize(20);
        articleService.listNewestArticles(page);
    }

    private void cacheSub() {
        List<Category> categories = categoryRepository.getAllCategories();
        for (Category category : categories) {
            ArticlePageParam param = new ArticlePageParam(category.getId(), null);
            param.setPageNum(1);
            param.setPageSize(20);
            articleService.listNewestArticles(param);
        }
    }

}
