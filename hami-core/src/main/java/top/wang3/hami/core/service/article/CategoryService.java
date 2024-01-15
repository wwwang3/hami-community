package top.wang3.hami.core.service.article;

import top.wang3.hami.common.dto.article.CategoryDTO;
import top.wang3.hami.common.model.Category;

import java.util.List;
import java.util.Map;

public interface CategoryService {

    List<Category> getAllCategories();

    Map<Integer, Category> getCategroyMap();

    CategoryDTO getCategoryDTOById(Integer id);

}
