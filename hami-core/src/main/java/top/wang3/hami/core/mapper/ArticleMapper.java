package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.lang.NonNull;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.model.ArticleCount;

import java.util.Collection;
import java.util.List;

@Mapper
@SuppressWarnings("unused")
public interface ArticleMapper extends BaseMapper<Article> {


    Article selectArticleById(@Param("id") Integer id);

    List<Article> selectArticleList(@Param("ids") Collection<Integer> ids);

    List<Integer> searchArticle(Page<Article> page, @Param("keyword") String keyword, @Param("dateTime") String dateTime);

    List<Integer> searchArticleByFulltextIndex(Page<Article> page, @Param("keyword") String keyword);

    @Select("""
                select user_id from article where id = #{articleId};
            """)
    Integer selectArticleAuthor(Integer id);

    List<Integer> selectFollowUserArticles(Page<Article> page, @Param("user_id") int loginUserId);

    List<Article> scanArticleAsc(@Param("lastId") int lastId, @Param("batchSize") int batchSize);

    List<Article> scanArticleDesc(@Param("maxId") int maxId, @Param("batchSize") int batchSize);

    @Select("""
                select id, content from article
                where id > #{lastId} and deleted = 0
                order by id
                limit #{batchSize};
            """)
    List<Article> scanArticleContent(@Param("lastId") int lastId, @Param("batchSize") int batchSize);

    @NonNull
    List<ArticleCount> selectCateArticleCount();

    Integer selectTotalArticleCount();

}