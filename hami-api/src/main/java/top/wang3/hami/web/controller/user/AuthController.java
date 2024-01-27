package top.wang3.hami.web.controller.user;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.dto.captcha.CaptchaType;
import top.wang3.hami.common.dto.user.RegisterParam;
import top.wang3.hami.common.dto.user.ResetPassParam;
import top.wang3.hami.core.service.account.AccountService;
import top.wang3.hami.core.service.captcha.CaptchaService;
import top.wang3.hami.security.model.Result;
import top.wang3.hami.web.annotation.Public;


/**
 * account
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final CaptchaService captchaService;
    private final AccountService accountService;


    /**
     * 发送验证码
     *
     * @param email 邮箱
     * @param type  类型 0,1,2
     * @return 空
     */
    @Public
    @GetMapping("/captcha")
    public Result<Void> sendCaptcha(@RequestParam("email") @NotBlank @Email String email,
                                    @RequestParam("type") @Pattern(regexp = "([012])") String type) {
        CaptchaType captchaType = CaptchaType.values()[Integer.parseInt(type)];
        // 6位验证码 有效期五分钟
        captchaService.sendCaptcha(captchaType, email);
        return Result.success("发送成功");
    }

    /**
     * 注册用户
     *
     * @param param {@link RegisterParam}
     * @return 空
     */
    @Public
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid
                                 RegisterParam param) {
        return Result.ofTrue(accountService.register(param))
                .orElse("注册失败");
    }

    /**
     * 更新密码
     *
     * @param param {@link ResetPassParam}
     * @return 空
     */
    @PostMapping("/update-pass")
    public Result<Void> updatePassword(@RequestBody @Valid ResetPassParam param) {
        return Result.ofTrue(accountService.updatePassword(param))
                .orElse("修改失败");
    }

    /**
     * 重置密码
     * 忘记密码后重置密码
     *
     * @param param {@link ResetPassParam}
     * @return 空
     */
    @Public
    @PostMapping("/reset-pass")
    public Result<Void> resetPassword(@RequestBody @Valid ResetPassParam param) {
        return Result.ofTrue(accountService.resetPassword(param))
                .orElse("重置失败");
    }
}
