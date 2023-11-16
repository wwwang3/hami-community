package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import top.wang3.hami.common.dto.article.ArticleStatDTO;
import top.wang3.hami.common.dto.user.UserStat;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.model.HotCounter;

import java.util.List;
import java.util.Map;

@Mapper
public interface ArticleStatMapper extends BaseMapper<ArticleStat> {

    @Select("""
        select article_id, likes, views, comments, collects
        from article_stat
        where article_id > #{lastArticleId}
        order by article_id
        limit #{batchSize};
    """)
    @ResultMap(value = "ArticleStatDTOResultMap")
    List<ArticleStatDTO> scanBatchStats(@Param("lastArticleId") int lastArticle, @Param("batchSize") int batchSize);

    @MapKey(value = "userId")
    Map<Integer, UserStat> selectUserStatsByUserIds(@Param("userIds") List<Integer> userIds);

    UserStat selectUserStat(@Param("userId") int userId);

    List<HotCounter> selectHotArticlesByCateId(@Param("categoryId") Integer categoryId, @Param("date") long date);

    List<HotCounter> selectHotArticles();

    @MapKey("articleId")
    Map<Integer, ArticleStatDTO> selectArticleStatsByArticleIds(@Param("articleIds") List<Integer> articleIds);
}