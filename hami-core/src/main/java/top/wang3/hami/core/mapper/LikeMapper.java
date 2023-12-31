package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wang3.hami.common.model.LikeItem;

import java.util.List;

@Mapper
public interface LikeMapper extends BaseMapper<LikeItem> {

    Long batchInsertLikeItem(@Param("items") List<LikeItem> items);
}