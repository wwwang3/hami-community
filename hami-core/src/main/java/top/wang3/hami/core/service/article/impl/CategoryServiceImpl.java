package top.wang3.hami.core.service.article.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.model.Category;
import top.wang3.hami.core.mapper.CategoryMapper;
import top.wang3.hami.core.service.article.CategoryService;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
        implements CategoryService {

    @Cacheable(cacheNames = "HAMI_CACHE_", key = "'CATEGORY_LIST'")
    @Override
    public List<Category> getAllCategories() {
        return super.list();
    }
}
