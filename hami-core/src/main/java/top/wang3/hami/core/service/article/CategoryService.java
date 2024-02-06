package top.wang3.hami.core.service.article;

import top.wang3.hami.common.model.Category;

import java.util.List;

public interface CategoryService {

    List<Category> getAllCategories();

    Category getCategoryById(Integer id);

}
