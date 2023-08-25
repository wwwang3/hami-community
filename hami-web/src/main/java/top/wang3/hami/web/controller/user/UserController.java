package top.wang3.hami.web.controller.user;


import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.wang3.hami.common.converter.UserConverter;
import top.wang3.hami.common.dto.AccountInfo;
import top.wang3.hami.common.dto.UserProfile;
import top.wang3.hami.common.dto.request.UserProfileParam;
import top.wang3.hami.common.model.User;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.core.service.account.AccountService;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.security.model.Result;

@RestController
@RequestMapping("/api/v1/user")
@Slf4j
public class UserController {

    private final UserService userService;

    private final AccountService accountService;

    public UserController(UserService userService, AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    @GetMapping("/profile")
    public Result<UserProfile> getUserProfile() {
        UserProfile profile = userService.getUserProfile();
        return Result.success(profile);
    }

    @GetMapping("/account/info")
    public Result<AccountInfo> getAccountInfo() {
        AccountInfo accountInfo = accountService.getAccountInfo();
        return Result.success(accountInfo);
    }

    @PostMapping("/avatar/upload")
    public Result<String> updateAvatar(@RequestParam("avatar") MultipartFile avatar) {
        String url = userService.updateAvatar(avatar);
        return Result.successData(url);
    }

    @PostMapping("/update")
    public Result<Void> updateUserProfile(@RequestBody @Valid
                                          UserProfileParam userProfileParam) {
        String username = userProfileParam.getUsername();
        if (StringUtils.hasText(username)) {
            boolean exists = accountService.checkUsername(userProfileParam.getUsername());
            if (exists) {
                //用户名已存在
                throw new ServiceException("用户名已被使用");
            }
        }
        User user = UserConverter.INSTANCE.toUser(userProfileParam);
        boolean success = userService.updateProfile(user);
        return success ? Result.success() : Result.error("更新失败");
    }
}
