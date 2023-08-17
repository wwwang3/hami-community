package top.wang3.hami.core.service.account.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.RegisterParam;
import top.wang3.hami.common.model.Account;
import top.wang3.hami.common.model.User;
import top.wang3.hami.core.exception.CaptchaServiceException;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.core.mapper.AccountMapper;
import top.wang3.hami.core.mapper.UserMapper;
import top.wang3.hami.core.service.account.AccountService;
import top.wang3.hami.core.service.captcha.impl.EmailCaptchaService;

@Service
@Slf4j
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account>
        implements AccountService {

    private final EmailCaptchaService captchaService;

    private final UserMapper userMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    public AccountServiceImpl(EmailCaptchaService captchaService, UserMapper userMapper) {
        this.captchaService = captchaService;
        this.userMapper = userMapper;
    }

    @Override
    public Account getAccountByEmailOrUsername(String account) {
        var wrapper = Wrappers.lambdaQuery(Account.class)
                .eq(Account::getUsername, account)
                .or()
                .eq(Account::getEmail, account);
        return super.getOne(wrapper);
    }

    @Override
    public void register(RegisterParam param) throws SecurityException {
        //校验验证码
        String email = param.getEmail();
        String username = param.getUsername();
        String captcha = param.getCaptcha();
        if (!captchaService.verify(Constants.REGISTER_EMAIL_CAPTCHA, email, captcha)) {
            throw new CaptchaServiceException("验证码无效或过期");
        }
        //判断用户名和邮箱是否被注册过
        if (checkUsername(param.getUsername())) {
            throw new ServiceException("用户名已被注册");
        }
        if (checkEmail(email)) {
            throw new ServiceException("邮箱已被注册");
        }
        //删除验证码
        captchaService.deleteCaptcha(Constants.REGISTER_EMAIL_CAPTCHA, email);
        //加密密码
        String encryptedPassword = passwordEncoder.encode(param.getPassword());
        Account account = Account.builder()
                .username(username)
                .email(email)
                .role("user")
                .state((byte) 1)
                .password(encryptedPassword)
                .build();
        super.save(account);
        userMapper.insert(new User(account.getId(), username));
    }

    public boolean checkUsername(String username) {
        var wrapper = Wrappers.query(Account.class)
                .eq("username", username);
        return super.getBaseMapper().exists(wrapper);
    }

    public boolean checkEmail(String email) {
        var wrapper = Wrappers.query(Account.class)
                .eq("email", email);
        return super.getBaseMapper().exists(wrapper);
    }

}
