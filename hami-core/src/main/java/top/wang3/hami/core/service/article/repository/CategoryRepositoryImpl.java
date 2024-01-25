package top.wang3.hami.core.service.article.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.Category;
import top.wang3.hami.core.mapper.CategoryMapper;

import java.util.List;

@Repository
public class CategoryRepositoryImpl extends ServiceImpl<CategoryMapper, Category>
        implements CategoryRepository {

    public static final String[] FIELDS = {"id", "name", "path"};

    @Override
    @Cacheable(cacheNames = Constants.CAFFEINE_CACHE_NAME, key = "'CATEGORY_LIST'",
            cacheManager = Constants.CaffeineCacheManager)
    public List<Category> getAllCategories() {
        return ChainWrappers.queryChain(getBaseMapper())
                .select(FIELDS)
                .list();
    }

    @Override
    @Cacheable(cacheNames = Constants.CAFFEINE_CACHE_NAME, key = "'CATEGORY_' + #id",
            cacheManager = Constants.CaffeineCacheManager)
    public Category getCategoryById(Integer id) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select(FIELDS)
                .eq("id", id)
                .one();
    }
}
