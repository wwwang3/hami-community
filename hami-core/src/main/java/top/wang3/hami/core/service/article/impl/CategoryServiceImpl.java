package top.wang3.hami.core.service.article.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.model.Category;
import top.wang3.hami.core.service.article.CategoryService;
import top.wang3.hami.core.service.article.repository.CategoryRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.getAllCategories();
    }

    @Override
    public Category getCategoryById(Integer id) {
        return categoryRepository.getCategoryById(id);
    }
}
