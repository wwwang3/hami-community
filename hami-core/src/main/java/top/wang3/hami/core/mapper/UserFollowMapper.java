package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wang3.hami.common.dto.interact.FollowCountItem;
import top.wang3.hami.common.model.UserFollow;

import java.util.List;

@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {


    List<FollowCountItem> selectUserFollowingCount(@Param("userIds") List<Integer> userIds);

    List<FollowCountItem> selectUserFollowerCount(@Param("userIds") List<Integer> userIds);

    Long batchInsertFollowItem(@Param("items") List<UserFollow> items);
}