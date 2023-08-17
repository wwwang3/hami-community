package top.wang3.hami.web.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.RegisterParam;
import top.wang3.hami.core.service.account.AccountService;
import top.wang3.hami.core.service.captcha.CaptchaService;
import top.wang3.hami.security.model.Result;

import java.util.concurrent.TimeUnit;

@Validated
@RestController
@RequestMapping("/api/v1/auth")
public class AccountController {

    private final AccountService accountService;

    private final CaptchaService captchaService;

    public AccountController(AccountService accountService, CaptchaService captchaService) {
        this.accountService = accountService;
        this.captchaService = captchaService;
    }

    @GetMapping("/captcha")
    public Result<Void> getRegisterCaptcha(@RequestParam("email") @Email String email,
                                           @RequestParam("type") @Pattern(regexp = "(register|reset)") String type) {
        String captchaType = resolveType(type);
        //6位验证码 有效期五分钟
        captchaService.sendCaptcha(captchaType, email, 6, TimeUnit.MINUTES.toSeconds(5));
        return Result.success("发送成功");
    }


    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid
                                     RegisterParam param) {
        accountService.register(param);
        return Result.success("注册成功");
    }

    public Result<Void> resetPassword() {
        //todo
//        accountService.resetPassword()
        return Result.success("重置密码成功");
    }


    private String resolveType(String type) {
        return "register".equals(type) ? Constants.REGISTER_EMAIL_CAPTCHA : Constants.RESET_EMAIL_CAPTCHA;
    }

}
