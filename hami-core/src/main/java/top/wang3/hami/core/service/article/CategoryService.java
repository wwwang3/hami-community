package top.wang3.hami.core.service.article;

import top.wang3.hami.common.model.Category;

import java.util.List;
import java.util.Map;

public interface CategoryService {

    List<Category> getAllCategories();

    Map<Integer, Category> getCategoryMap();

    Category getCategoryById(Integer id);

}
