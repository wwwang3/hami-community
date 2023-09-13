package top.wang3.hami.core.service.article.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.converter.ArticleConverter;
import top.wang3.hami.common.dto.CategoryDTO;
import top.wang3.hami.common.model.Category;
import top.wang3.hami.core.mapper.CategoryMapper;
import top.wang3.hami.core.service.article.CategoryService;

import java.util.List;

@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
        implements CategoryService {

    @Cacheable(cacheNames = Constants.CAFFEINE_CACHE_NAME, key = "'CATEGORY_LIST'", cacheManager = Constants.CaffeineCacheManager)
    @Override
    public List<Category> getAllCategories() {
        return super.list();
    }

    @Cacheable(cacheNames = Constants.CAFFEINE_CACHE_NAME, key = "'cate:'+#id", cacheManager = Constants.CaffeineCacheManager)
    @Override
    public CategoryDTO getCategoryDTOById(Integer id) {
        Category category = super.getById(id);
        return ArticleConverter.INSTANCE.toCategoryDTO(category);
    }
}
