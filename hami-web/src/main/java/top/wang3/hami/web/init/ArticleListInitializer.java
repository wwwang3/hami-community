package top.wang3.hami.web.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import top.wang3.hami.common.dto.request.ArticlePageParam;
import top.wang3.hami.common.model.Category;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.article.repository.CategoryRepository;

import java.util.List;

//@Component
@Slf4j
@Order(3)
@RequiredArgsConstructor
public class ArticleListInitializer implements ApplicationRunner {

    private final ArticleService articleService;
    private final CategoryRepository categoryRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            log.info("start to init cate-article-list");
            long start = System.currentTimeMillis();
            cacheTotal();
//            cacheSub();
            long end = System.currentTimeMillis();
            log.info("finish init cate-article-list, cost: {}ms", end - start);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
