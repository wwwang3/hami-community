package top.wang3.hami.web.controller.user;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.wang3.hami.common.converter.UserConverter;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.dto.request.UserProfileParam;
import top.wang3.hami.common.dto.user.AccountInfo;
import top.wang3.hami.common.dto.user.UserDTO;
import top.wang3.hami.common.dto.user.UserProfile;
import top.wang3.hami.common.model.LoginRecord;
import top.wang3.hami.common.model.User;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.core.service.account.AccountService;
import top.wang3.hami.core.service.account.LoginRecordService;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.security.model.Result;

@RestController
@RequestMapping("/api/v1/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final AccountService accountService;

    private final LoginRecordService loginRecordService;


    @GetMapping("/profile")
    public Result<UserProfile> getUserProfile() {
        UserProfile profile = userService.getUserProfile();
        return Result.successData(profile);
    }

    @GetMapping("/account/info")
    public Result<AccountInfo> getAccountInfo() {
        AccountInfo accountInfo = accountService.getAccountInfo();
        return Result.successData(accountInfo);
    }


    @GetMapping("/author_info/{id}")
    public Result<UserDTO> getAuthorInfo(@PathVariable(name = "id") Integer userId) {
        UserDTO user = userService.getAuthorInfoById(userId);
        return Result.ofNullable(user)
                .orElse("用户不存在");
    }

    @GetMapping("/login/log")
    public Result<PageData<LoginRecord>> getLoginRecords(@RequestParam("pageNum") long pageNum,
                                                         long pageSize) {
        PageData<LoginRecord> records = loginRecordService.getRecordsByPage(new PageParam(pageNum, pageSize));
        return Result.successData(records);
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
        return Result.successIfTrue(success, "error");
    }

}
