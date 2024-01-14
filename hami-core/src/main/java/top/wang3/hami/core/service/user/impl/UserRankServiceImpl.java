package top.wang3.hami.core.service.user.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.dto.user.UserDTO;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.common.vo.user.AuthorRankDTO;
import top.wang3.hami.core.service.user.UserRankService;
import top.wang3.hami.core.service.user.UserService;

import java.util.Collection;
import java.util.List;
import java.util.Set;


@Component
@RequiredArgsConstructor
public class UserRankServiceImpl implements UserRankService {

    private final UserService userService;

    @Override
    public List<AuthorRankDTO> getAuthorRankList() {
        String key = RedisConstants.AUTHOR_RANKING;
        // 数据不多直接读
        List<AuthorRankDTO> dtos = getRankList(key);
        List<Integer> userIds = ListMapperHandler.listTo(dtos, AuthorRankDTO::getUserId);
        Collection<UserDTO> userDTOS = userService.listAuthorInfoById(userIds, null);
        ListMapperHandler.doAssemble(
                dtos,
                AuthorRankDTO::getUserId,
                userDTOS,
                UserDTO::getUserId,
                AuthorRankDTO::setUser
        );
        return dtos;
    }

    private List<AuthorRankDTO> getRankList(String key) {
        Set<ZSetOperations.TypedTuple<Integer>> items = RedisClient.zRevRangeWithScore(key, 0, -1);
        return ListMapperHandler.listTo(items, item -> {
            AuthorRankDTO dto = new AuthorRankDTO();
            dto.setUserId(item.getValue());
            dto.setHotIndex(item.getScore());
            return dto;
        });
    }

}
