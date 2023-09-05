package top.wang3.hami.web.controller.user;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wang3.hami.common.dto.LoginProfile;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.security.model.Result;

@RestController
@RequestMapping("/api/v1/nav")
public class NavController {

    private final UserService userService;

    public NavController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public Result<LoginProfile> getLoProfile() {
        LoginProfile profile = userService.getLoginProfile();
        return Result.successData(profile);
    }
}
