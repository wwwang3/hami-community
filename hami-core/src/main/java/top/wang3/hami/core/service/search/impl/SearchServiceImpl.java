package top.wang3.hami.core.service.search.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.SearchParam;
import top.wang3.hami.common.dto.article.ArticleDTO;
import top.wang3.hami.common.dto.article.ArticleInfo;
import top.wang3.hami.common.dto.builder.ArticleOptionsBuilder;
import top.wang3.hami.common.message.SearchRabbitMessage;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.article.repository.ArticleRepository;
import top.wang3.hami.core.service.search.SearchService;

import java.util.List;
import java.util.regex.Pattern;

import static top.wang3.hami.common.constant.Constants.Hi_POST_TAG;
import static top.wang3.hami.common.constant.Constants.Hi_PRE_TAG;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ArticleRepository articleRepository;
    private final ArticleService articleService;
    private final RabbitMessagePublisher rabbitMessagePublisher;

    @Override
    public PageData<ArticleDTO> searchArticle(SearchParam param) {
        String keyword = param.getKeyword();
        rabbitMessagePublisher.publishMsg(new SearchRabbitMessage(keyword));
        Page<Article> page = param.toPage();
        List<Integer> ids = articleRepository.searchArticle(page, keyword);
        List<ArticleDTO> articles = articleService.listArticleById(ids, new ArticleOptionsBuilder());
        highlightKeyword(articles, keyword);
        return PageData.<ArticleDTO>builder()
                .total(page.getTotal())
                .pageNum(page.getCurrent())
                .data(articles)
                .build();
    }

    @Override
    public List<String> getHotSearch() {
        String key = RedisConstants.HOT_SEARCH;
        return RedisClient.zRevPage(key, 1, 20);
    }

    private void highlightKeyword(List<ArticleDTO> articles, String keyword) {
        Pattern pattern = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE);
        String replaced = Hi_PRE_TAG + keyword + Hi_POST_TAG;
        List<ArticleInfo> infos = ListMapperHandler.listTo(articles, ArticleDTO::getArticleInfo, false);
        infos.forEach(article -> {
            String title = pattern.matcher(article.getTitle()).replaceAll(replaced);
            String summary = pattern.matcher(article.getSummary()).replaceAll(replaced);
            article.setTitle(title);
            article.setSummary(summary);
        });
    }
}
