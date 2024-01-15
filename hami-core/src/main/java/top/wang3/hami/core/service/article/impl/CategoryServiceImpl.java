package top.wang3.hami.core.service.article.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.converter.ArticleConverter;
import top.wang3.hami.common.dto.article.CategoryDTO;
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
    @Cacheable(cacheNames = Constants.CAFFEINE_CACHE_NAME, key = "'CATEGORY_LIST'",
            cacheManager = Constants.CaffeineCacheManager)
    public List<Category> getAllCategories() {
        return categoryRepository.getAllCategories();
    }

    @Override
    @Cacheable(cacheNames = Constants.CAFFEINE_CACHE_NAME, key = "'CATEGORY_MAP'",
            cacheManager = Constants.CaffeineCacheManager)
    public Map<Integer, Category> getCategroyMap() {
        List<Category> categories = categoryRepository.getAllCategories();
        return ListMapperHandler.listToMap(categories, Category::getId);
    }

    @Cacheable(cacheNames = Constants.CAFFEINE_CACHE_NAME, key = "'cate:'+#id",
            cacheManager = Constants.CaffeineCacheManager)
    @Override
    public CategoryDTO getCategoryDTOById(Integer id) {
        Category category = categoryRepository.getCategoryById(id);
        return ArticleConverter.INSTANCE.toCategoryDTO(category);
    }
}
