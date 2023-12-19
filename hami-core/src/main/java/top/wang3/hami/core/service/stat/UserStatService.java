package top.wang3.hami.core.service.stat;


import top.wang3.hami.common.dto.stat.UserStatDTO;
import top.wang3.hami.common.model.UserStat;

import java.util.List;

public interface UserStatService {

    UserStatDTO getUserStatDTOById(Integer userId);

    List<UserStatDTO> getUserStatDTOByIds(List<Integer> userIds);

    List<UserStat> getUserStatByIds(List<Integer> useIds);
}
