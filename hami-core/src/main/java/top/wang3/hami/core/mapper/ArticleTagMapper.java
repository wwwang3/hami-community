package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wang3.hami.common.dto.article.ArticleTagDTO;
import top.wang3.hami.common.model.ArticleTag;

import java.util.List;

@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {


    List<ArticleTagDTO> getArticleTagByArticleIds(@Param("articleIds") List<Integer> articleIds);

    Long batchInsertArticleTag(@Param("items") List<ArticleTag> items);
}