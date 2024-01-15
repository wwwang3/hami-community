package top.wang3.hami.core.service.search;


import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.SearchParam;
import top.wang3.hami.common.vo.article.ArticleVo;

import java.util.List;

public interface SearchService {

    PageData<ArticleVo> searchArticle(SearchParam param);

    List<String> getHotSearch();
}
