package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wang3.hami.common.model.LoginRecord;

import java.util.List;

@Mapper
public interface LoginRecordMapper extends BaseMapper<LoginRecord> {

    long batchInsertRecords(@Param("records") List<LoginRecord> records);
}