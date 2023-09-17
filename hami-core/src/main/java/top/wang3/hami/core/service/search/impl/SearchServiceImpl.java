package top.wang3.hami.core.service.search.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.dto.ArticleSearchDTO;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.UserDTO;
import top.wang3.hami.common.dto.request.SearchParam;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.repository.ArticleRepository;
import top.wang3.hami.core.service.article.CategoryService;
import top.wang3.hami.core.service.search.SearchService;
import top.wang3.hami.core.service.user.UserService;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ArticleRepository articleRepository;
    private final UserService userService;
    private final CategoryService categoryService;

    public static final String PRE_TAG = "<em>";
    public static final String POST_TAG = "</em>";

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
        for (ArticleSearchDTO article : articles) {
            String title = article.getTitle().replaceAll(keyword, PRE_TAG + keyword + POST_TAG);
            String summary = article.getSummary().replaceAll(keyword, PRE_TAG + keyword + POST_TAG);
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
        List<UserDTO> users = userService.getAuthorInfoByIds(userIds, new UserService.OptionsBuilder().noStat().noFollowState());
        ListMapperHandler.doAssemble(articles, ArticleSearchDTO::getUserId, users,
                UserDTO::getUserId, ArticleSearchDTO::setAuthor);
    }
}
