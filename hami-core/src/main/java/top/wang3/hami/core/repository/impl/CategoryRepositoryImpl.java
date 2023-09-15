package top.wang3.hami.core.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Repository;
import top.wang3.hami.common.model.Category;
import top.wang3.hami.core.mapper.CategoryMapper;
import top.wang3.hami.core.repository.CategoryRepository;

import java.util.List;

@Repository
public class CategoryRepositoryImpl extends ServiceImpl<CategoryMapper, Category>
        implements CategoryRepository {

    @Override
    public List<Category> getAllCategories() {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("id", "name")
                .list();
    }

    @Override
    public Category getCategoryById(Integer id) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("id", "name")
                .eq("id", id)
                .one();
    }
}
