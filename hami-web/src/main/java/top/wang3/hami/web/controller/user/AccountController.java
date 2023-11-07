package top.wang3.hami.web.controller.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.converter.UserConverter;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.dto.user.AccountInfo;
import top.wang3.hami.common.dto.user.LoginProfile;
import top.wang3.hami.common.dto.user.UserProfileParam;
import top.wang3.hami.common.model.LoginRecord;
import top.wang3.hami.common.model.User;
import top.wang3.hami.core.exception.HamiServiceException;
import top.wang3.hami.core.service.account.AccountService;
import top.wang3.hami.core.service.account.LoginRecordService;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.security.model.Result;

@Validated
@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;
    private final LoginRecordService loginRecordService;

    @GetMapping("/me")
    public Result<LoginProfile> me() {
        LoginProfile loginProfile = userService.getLoginProfile();
        return Result.of(loginProfile);
    }

    @GetMapping("/info")
    public Result<AccountInfo> getAccountInfo() {
        AccountInfo info = accountService.getAccountInfo();
        return Result.successData(info);
    }

    @PostMapping("/update")
    public Result<Void> updateUserProfile(@RequestBody @Valid
                                          UserProfileParam userProfileParam) {
        String username = userProfileParam.getUsername();
        if (StringUtils.hasText(username)) {
            boolean exists = accountService.checkUsername(userProfileParam.getUsername());
            if (exists) {
                //用户名已存在
                throw new HamiServiceException("用户名已被使用");
            }
        }
        User user = UserConverter.INSTANCE.toUser(userProfileParam);
        boolean success = userService.updateProfile(user);
        return Result.successIfTrue(success, "error");
    }

    @GetMapping("/login/log")
    public Result<PageData<LoginRecord>> getLoginRecords(@RequestParam("pageNum") long pageNum,
                                                         @RequestParam("pageSize") long pageSize) {
        PageData<LoginRecord> records = loginRecordService.getRecordsByPage(new PageParam(pageNum, pageSize));
        return Result.successData(records);
    }


}
