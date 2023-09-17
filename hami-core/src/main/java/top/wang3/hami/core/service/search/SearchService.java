package top.wang3.hami.core.service.search;


import top.wang3.hami.common.dto.ArticleSearchDTO;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.request.SearchParam;

public interface SearchService {

    PageData<ArticleSearchDTO> searchArticle(SearchParam param);
}
