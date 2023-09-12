package top.wang3.hami.web.init;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.model.Category;
import top.wang3.hami.common.model.Tag;
import top.wang3.hami.core.service.article.CategoryService;
import top.wang3.hami.core.service.article.TagService;

import java.util.List;

@Component
@Slf4j
public class LocalCacheInitializer implements ApplicationRunner {

    @Resource
    CategoryService categoryService;

    @Resource
    TagService tagService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            log.info("start to load local tag/category-cache");
            long start = System.currentTimeMillis();
            List<Tag> tags = tagService.getAllTags();
            tags.forEach(t -> {
                tagService.getTagById(t.getId());
            });
            List<Category> categories = categoryService.getAllCategories();
            categories.forEach(category -> {
                categoryService.getCategoryDTOById(category.getId());
            });
            long end = System.currentTimeMillis();
            log.info("success to load local tag/category-cache, cost: {}ms",  end - start);
        } catch (Exception e) {
            log.error("error to init tag/category-cache: error_class:{}, error_msg: {}",
                    e.getClass().getSimpleName(), e.getMessage());
        }

    }
}
