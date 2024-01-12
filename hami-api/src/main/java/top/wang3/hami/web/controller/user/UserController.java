package top.wang3.hami.web.controller.user;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wang3.hami.common.dto.user.AuthorRankDTO;
import top.wang3.hami.common.dto.user.UserDTO;
import top.wang3.hami.core.service.user.UserRankService;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.security.model.Result;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final UserRankService userRankService;

    @GetMapping("/info/{id}")
    public Result<UserDTO> getAuthorInfo(@PathVariable(name = "id") Integer userId) {
        UserDTO user = userService.getAuthorInfoById(userId);
        return Result.ofNullable(user)
                .orElse("用户不存在");
    }

    @GetMapping("/rank/list")
    public Result<List<AuthorRankDTO>> getAuthorRankList() {
        List<AuthorRankDTO> list = userRankService.getAuthorRankList();
        return Result.successData(list);
    }
}
