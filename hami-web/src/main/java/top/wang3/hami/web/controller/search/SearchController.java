package top.wang3.hami.web.controller.search;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wang3.hami.common.dto.ArticleSearchDTO;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.request.SearchParam;
import top.wang3.hami.core.service.search.SearchService;
import top.wang3.hami.security.model.Result;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {


    private final SearchService searchService;

    @PostMapping("article")
    public Result<PageData<ArticleSearchDTO>> searArticle(@RequestBody
                                                          @Valid SearchParam param) {
        PageData<ArticleSearchDTO> data = searchService.searchArticle(param);
        return Result.successData(data);
    }

}
