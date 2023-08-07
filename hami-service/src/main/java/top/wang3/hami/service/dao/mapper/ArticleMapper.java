package top.wang3.hami.service.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wang3.hami.common.model.Article;

@Mapper
public interface ArticleMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Article record);

    int insertSelective(Article record);

    Article selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Article record);

    int updateByPrimaryKey(Article record);
}