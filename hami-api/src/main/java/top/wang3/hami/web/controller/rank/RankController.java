package top.wang3.hami.web.controller.rank;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.wang3.hami.common.vo.article.HotArticle;
import top.wang3.hami.common.vo.user.HotAuthor;
import top.wang3.hami.core.service.rank.RankListService;
import top.wang3.hami.security.annotation.PublicApi;
import top.wang3.hami.security.model.Result;

import java.util.List;


/**
 * rank
 * 排行榜
 */
@RestController
@RequestMapping("/api/v1/rank")
@RequiredArgsConstructor
public class RankController {

    private final RankListService rankListService;

    /**
     * 获取热门文章
     *
     * @param categoryId 分类Id
     * @return {@link List<HotArticle>}
     */
    @PublicApi
    @GetMapping("/hot/article")
    public Result<List<HotArticle>> listHotArticle(@RequestParam(value = "cateId", required = false)
                                                   Integer categoryId) {
        List<HotArticle> articles = rankListService.listHotArticle(categoryId);
        return Result.successData(articles);
    }

    /**
     * 获取作者排行榜
     *
     * @return {@link List<HotAuthor>}
     */
    @PublicApi
    @GetMapping("/hot/author")
    public Result<List<HotAuthor>> listHotAuthor() {
        List<HotAuthor> articles = rankListService.listHotAuthor();
        return Result.successData(articles);
    }
}
