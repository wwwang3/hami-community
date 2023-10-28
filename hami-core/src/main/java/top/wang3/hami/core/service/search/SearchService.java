package top.wang3.hami.core.service.search;


import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.article.ArticleDTO;
import top.wang3.hami.common.dto.request.SearchParam;

import java.util.List;

public interface SearchService {

    PageData<ArticleDTO> searchArticle(SearchParam param);

    List<String> getHotSearch();
}
