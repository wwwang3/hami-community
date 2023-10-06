package top.wang3.hami.core.service.article.repository;


import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.model.Category;

import java.util.List;

public interface CategoryRepository extends IService<Category> {

    List<Category> getAllCategories();

    Category getCategoryById(Integer id);
}
