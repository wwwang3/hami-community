package top.wang3.hami.core.service.user;

import top.wang3.hami.common.dto.user.AuthorRankDTO;

import java.util.List;

public interface UserRankService {

    List<AuthorRankDTO> getAuthorRankList();
}
