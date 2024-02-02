package top.wang3.hami.web.controller.search;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.SearchParam;
import top.wang3.hami.common.vo.article.ArticleVo;
import top.wang3.hami.core.service.search.SearchService;
import top.wang3.hami.security.annotation.PublicApi;
import top.wang3.hami.security.model.Result;

import java.util.List;

/**
 * search
 */
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {


    private final SearchService searchService;

    /**
     * 文章搜索
     *
     * @param param {@link SearchParam}
     * @return {@link PageData<ArticleVo>}
     */
    @PublicApi
    @PostMapping("/query_list")
    public Result<PageData<ArticleVo>> searchArticle(@RequestBody @Valid SearchParam param) {
        if (!StringUtils.hasText(param.getKeyword())) {
            return Result.of(PageData.empty());
        }
        PageData<ArticleVo> data = searchService.searchArticle(param);
        return Result.successData(data);
    }

    /**
     * 热搜列表
     *
     * @return 热搜关键字列表 {@link List<String>}
     */
    @PublicApi
    @GetMapping("/hot")
    public Result<List<String>> getHotSearch() {
        List<String> hots = searchService.getHotSearch();
        return Result.successData(hots);
    }

}
