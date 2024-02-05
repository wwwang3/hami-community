package top.wang3.hami.core.service.search.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.SearchParam;
import top.wang3.hami.common.dto.builder.ArticleOptionsBuilder;
import top.wang3.hami.common.message.SearchRabbitMessage;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.common.vo.article.ArticleVo;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.article.repository.ArticleRepository;
import top.wang3.hami.core.service.search.SearchService;

import java.util.List;

@Service
@Primary
@RequiredArgsConstructor
public class MySQLFulltextIndexSearchService implements SearchService {

    private final RabbitMessagePublisher rabbitMessagePublisher;
    private final ArticleService articleService;
    private final ArticleRepository articleRepository;

    @Override
    public PageData<ArticleVo> searchArticle(SearchParam param) {
        String keyword = param.getKeyword();
        rabbitMessagePublisher.publishMsg(new SearchRabbitMessage(keyword));
        Page<Article> page = param.toPage(false);
        List<Integer> ids = articleRepository.searchArticleByFulltextIndex(page, keyword);
        List<ArticleVo> articles = articleService.listArticleVoById(ids, new ArticleOptionsBuilder());
        page.setTotal(-1);
        return PageData.<ArticleVo>builder()
                .total(page.getTotal())
                .current(page.getCurrent())
                .data(articles)
                .build();
    }

    @Override
    public List<String> getHotSearch() {
        String key = RedisConstants.HOT_SEARCH;
        return RedisClient.zRevPage(key, 1, 20);
    }
}
