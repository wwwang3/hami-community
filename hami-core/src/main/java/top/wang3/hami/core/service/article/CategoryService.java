package top.wang3.hami.core.service.article;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.model.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {
    List<Category> getAllCategories();
}
