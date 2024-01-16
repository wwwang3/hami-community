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
import java.util.Map;

import static top.wang3.hami.core.init.InitializerEnums.LOCAL_CACHE;

@Component
@Slf4j
@Order(1)
public class LocalCacheInitializer implements HamiInitializer {


    @Override
    public InitializerEnums getName() {
        return LOCAL_CACHE;
    }

    @Override
    public boolean alwaysExecute() {
        return true;
    }

    @Resource
    CategoryService categoryService;

    @Resource
    TagService tagService;

    @Override
    @SuppressWarnings("unused")
    public void run() {
        List<Tag> tags = tagService.getAllTag();
        Map<Integer, Tag> map = tagService.getTagMap();
        List<Category> categories = categoryService.getAllCategories();
        Map<Integer, Category> categroyMap = categoryService.getCategroyMap();
    }
}
