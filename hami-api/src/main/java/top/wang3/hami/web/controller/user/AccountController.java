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


/**
 * account
 */
@Validated
@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;
    private final LoginRecordService loginRecordService;

    /**
     * 获取当前登录用户信息
     *
     * @return {@link LoginProfile}
     */
    @GetMapping("/me")
    public Result<LoginProfile> me() {
        LoginProfile loginProfile = userService.getLoginProfile();
        return Result.of(loginProfile);
    }

    /**
     * 获取账户信息
     *
     * @return {@link AccountInfo}
     */
    @GetMapping("/info")
    public Result<AccountInfo> getAccountInfo() {
        AccountInfo info = accountService.getAccountInfo();
        return Result.successData(info);
    }

    /**
     * 更新个人资料
     *
     * @param param {@link UserProfileParam}
     * @return 空
     */
    @PostMapping("/update")
    public Result<Void> updateUserProfile(@RequestBody @Valid
                                          UserProfileParam param) {
        boolean success = accountService.updateProfile(param);
        return Result.successIfTrue(success, "操作失败");
    }

    /**
     * 获取登录记录
     *
     * @param current 当前页数
     * @param size    元素个数
     * @return {@link PageData<LoginRecord>}
     */
    @GetMapping("/login/log")
    public Result<PageData<LoginRecord>> getLoginRecords(@RequestParam("current") long current,
                                                         @RequestParam("size") long size) {
        PageData<LoginRecord> records = loginRecordService.listLoginRecordByPage(new PageParam(current, size));
        return Result.successData(records);
    }


}
