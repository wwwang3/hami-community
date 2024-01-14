package top.wang3.hami.web.controller.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.dto.user.UserProfileParam;
import top.wang3.hami.common.model.LoginRecord;
import top.wang3.hami.common.vo.user.AccountInfo;
import top.wang3.hami.common.vo.user.LoginProfile;
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
        boolean success = accountService.updateProfile(userProfileParam);
        return Result.successIfTrue(success, "操作失败");
    }

    @GetMapping("/login/log")
    public Result<PageData<LoginRecord>> getLoginRecords(@RequestParam("pageNum") long pageNum,
                                                         @RequestParam("pageSize") long pageSize) {
        PageData<LoginRecord> records = loginRecordService.getRecordsByPage(new PageParam(pageNum, pageSize));
        return Result.successData(records);
    }


}
