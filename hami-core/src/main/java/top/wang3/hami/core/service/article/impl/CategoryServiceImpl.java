package top.wang3.hami.core.service.article.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.Category;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.service.article.CategoryService;
import top.wang3.hami.core.service.article.repository.CategoryRepository;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.getAllCategories();
    }

    @Override
    public Map<Integer, Category> getCategoryMap() {
        return ListMapperHandler.listToMap(getAllCategories(), Category::getId);
    }

    @Override
    @Cacheable(cacheNames = Constants.CAFFEINE_CACHE_NAME, key = "'cate:'+#id",
            cacheManager = Constants.CaffeineCacheManager)
    public Category getCategoryById(Integer id) {
        return categoryRepository.getCategoryById(id);
    }
}
