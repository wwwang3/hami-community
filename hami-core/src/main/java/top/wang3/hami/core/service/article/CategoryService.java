package top.wang3.hami.core.service.article;

import top.wang3.hami.common.dto.CategoryDTO;
import top.wang3.hami.common.model.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();

    CategoryDTO getCategoryDTOById(Integer id);
}
