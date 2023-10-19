package top.wang3.hami.core.service.search.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.article.ArticleSearchDTO;
import top.wang3.hami.common.dto.builder.UserOptionsBuilder;
import top.wang3.hami.common.dto.request.SearchParam;
import top.wang3.hami.common.dto.user.UserDTO;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.service.article.CategoryService;
import top.wang3.hami.core.service.article.repository.ArticleRepository;
import top.wang3.hami.core.service.search.SearchService;
import top.wang3.hami.core.service.user.UserService;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static top.wang3.hami.common.constant.Constants.Hi_POST_TAG;
import static top.wang3.hami.common.constant.Constants.Hi_PRE_TAG;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ArticleRepository articleRepository;
    private final UserService userService;
    private final CategoryService categoryService;

    @Override
    public PageData<ArticleSearchDTO> searchArticle(SearchParam param) {
        Page<Article> page = param.toPage();
        List<ArticleSearchDTO> articles = articleRepository.searchArticle(page, param.getKeyword());
        highlightKeyword(articles, param.getKeyword());
        buildCategory(articles);
        buildAuthor(articles);
        return PageData.<ArticleSearchDTO>builder()
                .total(page.getTotal())
                .pageNum(page.getCurrent())
                .data(articles)
                .build();
    }

    private void highlightKeyword(List<ArticleSearchDTO> articles, String keyword) {
        Pattern pattern = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE);
        String replaced = Hi_PRE_TAG + keyword + Hi_POST_TAG;
        for (ArticleSearchDTO article : articles) {
            String title = pattern.matcher(article.getTitle()).replaceAll(replaced);
            String summary = pattern.matcher(article.getSummary()).replaceAll(replaced);
            article.setTitle(title);
            article.setSummary(summary);
        }
    }

    private void buildCategory(List<ArticleSearchDTO> articles) {
        for (ArticleSearchDTO article : articles) {
            article.setCategoryDTO(categoryService.getCategoryDTOById(article.getCategoryId()));
        }
    }

    private void buildAuthor(List<ArticleSearchDTO> articles) {
        List<Integer> userIds = ListMapperHandler.listTo(articles, ArticleSearchDTO::getUserId);
        Collection<UserDTO> users = userService.listAuthorInfoById(userIds, UserOptionsBuilder.justInfo());
        ListMapperHandler.doAssemble(articles, ArticleSearchDTO::getUserId, users,
                UserDTO::getUserId, ArticleSearchDTO::setAuthor);
    }
}
