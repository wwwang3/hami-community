package top.wang3.hami.core.service.stat.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.converter.StatConverter;
import top.wang3.hami.common.dto.stat.UserStatDTO;
import top.wang3.hami.common.model.UserStat;
import top.wang3.hami.core.service.stat.UserStatService;
import top.wang3.hami.core.service.stat.repository.UserStatRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserStatServiceImpl implements UserStatService {

    private final UserStatRepository userStatRepository;

    @Override
    public UserStatDTO getUserStatDTOById(Integer userId) {
        UserStat stat = userStatRepository.selectUserStatById(userId);
        return StatConverter.INSTANCE.toUserStatDTO(stat);
    }

    @Override
    public List<UserStatDTO> getUserStatDTOByIds(List<Integer> userIds) {
        List<UserStat> userStats = userStatRepository.selectUserStatByIds(userIds);
        return StatConverter.INSTANCE.toUserStatDTO(userStats);
    }

    @Override
    public List<UserStat> getUserStatByIds(List<Integer> useIds) {
        return userStatRepository.selectUserStatByIds(useIds);
    }

}
