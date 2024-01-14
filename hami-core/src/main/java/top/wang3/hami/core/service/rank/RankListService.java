package top.wang3.hami.core.service.rank;

import top.wang3.hami.common.vo.article.HotArticle;
import top.wang3.hami.common.vo.user.HotAuthor;

import java.util.List;

public interface RankListService {

    /**
     * 获取热门文章列表
     * @param cateId 分类ID
     * @return List<HotArticle> 热门文章列表
     */
    List<HotArticle> listHotArticle(Integer cateId);

    /**
     * 获取作者排行榜
     * @return List<HotArticle> 作者榜
     */
    List<HotAuthor> listHotAuthor();

}
