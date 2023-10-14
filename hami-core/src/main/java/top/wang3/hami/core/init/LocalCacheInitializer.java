package top.wang3.hami.core.init;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.model.Category;
import top.wang3.hami.common.model.Tag;
import top.wang3.hami.core.service.article.CategoryService;
import top.wang3.hami.core.service.article.TagService;

import java.util.List;

@Component
@Slf4j
@Order(1)
public class LocalCacheInitializer implements HamiInitializer {


    @Override
    public String getName() {
        return LOCAL_CACHE;
    }

    @Resource
    CategoryService categoryService;

    @Resource
    TagService tagService;

    @Override
    public void run() {
        List<Tag> tags = tagService.getAllTags();
        tags.forEach(t -> {
            tagService.getTagById(t.getId());
        });
        List<Category> categories = categoryService.getAllCategories();
        categories.forEach(category -> {
            categoryService.getCategoryDTOById(category.getId());
        });
    }
}
