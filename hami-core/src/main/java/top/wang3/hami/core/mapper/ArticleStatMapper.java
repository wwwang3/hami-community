package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.model.HotCounter;
import top.wang3.hami.common.model.UserStat;

import java.util.List;

@Mapper
public interface ArticleStatMapper extends BaseMapper<ArticleStat> {

    List<ArticleStat> scanArticleStatDesc(@Param("maxId") int maxId, @Param("batchSize") int batchSize);

    List<UserStat> scanUserStats(@Param("userIds") List<Integer> userIds);

    UserStat selectUserStat(@Param("userId") int userId);

    List<HotCounter> selectCateHotArticle(@Param("categoryId") Integer categoryId, @Param("datetime") String datetime);

    List<HotCounter> selectOverallHotArticle(@Param("datetime") String datetime);

    Long batchUpdateLikes(@Param("stats") List<ArticleStat> stats);
    Long batchUpdateComments(@Param("stats") List<ArticleStat> stats);
    Long batchUpdateCollects(@Param("stats") List<ArticleStat> stats);
    Long batchUpdateViews(@Param("stats") List<ArticleStat> stats);

    Long batchInsertArticleStat(@Param("items") List<ArticleStat> items);
}