package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.wang3.hami.common.model.Category;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}