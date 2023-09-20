package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.wang3.hami.common.dto.article.ArticleDTO;
import top.wang3.hami.common.dto.article.ArticleSearchDTO;
import top.wang3.hami.common.model.Article;

import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {


    @Select("""
        select count(id) from article where id = #{articleId} and deleted = 0;
    """)
    boolean isArticleExist(Integer articleId);

    List<ArticleDTO> selectArticlesByCategoryId(Page<Article> page, @Param("categoryId") Integer categoryId);

    @Select("""
        select user_id from article where id = #{articleId};
    """
    )
    Integer getArticleAuthorId(Integer articleId);

    List<ArticleSearchDTO> searchArticle(Page<Article> page, @Param("keyword") String keyword);

    @Select("""
        select user_id from article where id = #{articleId} and deleted = 0;
    """)
    Integer getArticleAuthor(Integer articleId);

    List<Integer> selectFollowUserArticles(Page<Article> page, @Param("user_id") int loginUserId);
}