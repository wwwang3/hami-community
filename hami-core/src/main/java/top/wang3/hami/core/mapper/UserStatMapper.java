package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wang3.hami.common.model.HotCounter;
import top.wang3.hami.common.model.UserStat;

import java.util.List;

@Mapper
public interface UserStatMapper extends BaseMapper<UserStat> {
    Long batchUpdateUserStat(@Param("stats") List<UserStat> userStats);

    Long batchInsertUserStat(@Param("stats") List<UserStat> stats);

    List<HotCounter> selectAuthorRankList();

    List<UserStat> scanUserStatDesc(@Param("maxId") int maxId, @Param("batchSize") int batchSize);
}