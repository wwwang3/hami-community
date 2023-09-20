package top.wang3.hami.core.service.article;

import top.wang3.hami.common.dto.article.HotArticleDTO;

import java.util.List;

/**
 * 文章排行榜
 */
public interface ArticleRankService {

    List<HotArticleDTO> getHotArticles(Integer categoryId);
}
