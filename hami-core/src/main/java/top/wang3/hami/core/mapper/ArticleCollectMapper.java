package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wang3.hami.common.model.ArticleCollect;

import java.util.List;

@Mapper
public interface ArticleCollectMapper extends BaseMapper<ArticleCollect> {

    Long batchInsertArticleCollect(@Param("items")List<ArticleCollect> items);
}