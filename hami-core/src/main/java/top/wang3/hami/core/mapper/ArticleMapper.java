package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.wang3.hami.common.dto.article.ArticleSearchDTO;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.model.ArticleDO;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {


    List<ArticleSearchDTO> searchArticle(Page<Article> page, @Param("keyword") String keyword);

    @Select("""
                select user_id from article where id = #{articleId} and deleted = 0;
            """)
    Integer getArticleAuthor(Integer articleId);

    List<Integer> selectFollowUserArticles(Page<Article> page, @Param("user_id") int loginUserId);

    ArticleDO selectArticleById(@Param("articleId") Integer articleId);

   List<ArticleDO> listArticleById(@Param("ids") Collection<Integer> ids);

    @Select("""
        select id from article
        where id > #{lastId} and deleted = 0
        order by id
        limit #{batchSize};
    """)
    List<Integer> scanArticleIds(@Param("lastId") int lastId, @Param("batchSize") int batchSize);

    List<ArticleDO> scanArticles(@Param("ids") List<Integer> ids);

}