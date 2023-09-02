package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import top.wang3.hami.common.model.Article;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {


    @Select("""
        select 1 from article where id = #{articleId} and deleted = 0;
    """)
    boolean isArticleExist(Integer articleId);
}