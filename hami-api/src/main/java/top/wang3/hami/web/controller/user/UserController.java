package top.wang3.hami.web.controller.user;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wang3.hami.common.vo.user.UserVo;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.security.annotation.PublicApi;
import top.wang3.hami.security.model.Result;

/**
 * user
 */
@RestController
@RequestMapping("/api/v1/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取作者信息
     *
     * @param id 用户Id
     * @return {@link UserVo}
     */
    @PublicApi
    @GetMapping("/info/{id}")
    public Result<UserVo> getAuthorInfo(@PathVariable(name = "id") Integer id) {
        UserVo user = userService.getAuthorInfoById(id);
        return Result.ofNullable(user)
                .orElse("用户不存在");
    }

}
