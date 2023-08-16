package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.wang3.hami.common.model.LoginRecord;

@Mapper
public interface LoginRecordMapper extends BaseMapper<LoginRecord> {
}