package top.wang3.hami.web.controller.user;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.dto.user.RegisterParam;
import top.wang3.hami.common.dto.user.ResetPassParam;
import top.wang3.hami.core.service.account.AccountService;
import top.wang3.hami.core.service.captcha.CaptchaService;
import top.wang3.hami.security.model.Result;

import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final CaptchaService captchaService;
    private final AccountService accountService;

    @GetMapping("/captcha")
    public Result<Void> getRegisterCaptcha(@RequestParam("email") @NotBlank @Email String email,
                                           @RequestParam("type") @Pattern(regexp = "(register|reset|update)") String type) {
        String captchaType = captchaService.resolveCaptchaType(type);
        //6位验证码 有效期五分钟
        captchaService.sendCaptcha(captchaType, email, 6, TimeUnit.MINUTES.toSeconds(5));
        return Result.success("发送成功");
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid
                                 RegisterParam param) {
        return Result.ofTrue(accountService.register(param))
                .orElse("注册失败");
    }

    @PostMapping("/update-pass")
    public Result<Void> updatePassword(@RequestBody @Valid ResetPassParam param) {
        return Result.ofTrue(accountService.updatePassword(param))
                .orElse("修改失败");
    }

    @PostMapping("/reset-pass")
    public Result<Void> resetPassword(@RequestBody @Valid ResetPassParam param) {
        return Result.ofTrue(accountService.resetPassword(param))
                .orElse("重置失败");
    }
}
