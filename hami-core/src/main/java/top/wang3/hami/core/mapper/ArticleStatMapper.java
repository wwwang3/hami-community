package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.wang3.hami.common.dto.user.UserStat;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.model.HotCounter;

import java.util.List;

@Mapper
public interface ArticleStatMapper extends BaseMapper<ArticleStat> {

    @Select("""
        select article_id, likes, views, comments, collects
        from article_stat
        where article_id > #{lastArticleId}
        order by article_id
        limit #{batchSize};
    """)
    List<ArticleStat> scanBatchStats(@Param("lastArticleId") int lastArticle, @Param("batchSize") int batchSize);

    List<UserStat> selectUserStatsByUserIds(@Param("userIds") List<Integer> userIds);

    UserStat selectUserStat(@Param("userId") int userId);

    List<HotCounter> selectHotArticlesByCateId(@Param("categoryId") Integer categoryId);

    List<HotCounter> selectHotArticles();
}