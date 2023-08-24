package top.wang3.hami.core.service.account.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import jakarta.annotation.Resource;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.converter.AccountConverter;
import top.wang3.hami.common.dto.AccountInfo;
import top.wang3.hami.common.dto.request.RegisterParam;
import top.wang3.hami.common.dto.request.ResetPassParam;
import top.wang3.hami.common.model.Account;
import top.wang3.hami.common.model.User;
import top.wang3.hami.core.exception.CaptchaServiceException;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.core.mapper.AccountMapper;
import top.wang3.hami.core.mapper.UserMapper;
import top.wang3.hami.core.service.account.AccountService;
import top.wang3.hami.core.service.captcha.impl.EmailCaptchaService;
import top.wang3.hami.security.context.LoginUserContext;
import top.wang3.hami.security.service.TokenService;

import java.util.function.Supplier;

@Service
@Slf4j
@Setter
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account>
        implements AccountService {

    private final EmailCaptchaService captchaService;

    private final UserMapper userMapper;

    @Resource
    PasswordEncoder passwordEncoder;

    @Resource
    TransactionTemplate transactionTemplate;

    @Resource
    TokenService tokenService;

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
        //验证完用户名再删除验证码
        captchaService.deleteCaptcha(Constants.REGISTER_EMAIL_CAPTCHA, email);
        if (checkEmail(email)) {
            throw new ServiceException("邮箱已被注册");
        }
        //加密密码
        String encryptedPassword = passwordEncoder.encode(param.getPassword());
        Account account = Account.builder()
                .username(username)
                .email(email)
                .role("user")
                .state((byte) 1)
                .password(encryptedPassword)
                .build();
        transactionTemplate.execute(status -> {
            super.save(account);
            return userMapper.insert(new User(account.getId(), username));
        });
    }

    @Override
    public void resetPassword(ResetPassParam param) {
        //校验验证码
        restPassword(param, "reset");
    }


    @Override
    public void updatePassword(ResetPassParam param) {
        //修改密码
        //清除用户所有的登录态
        boolean success = restPassword(param, "update");
        if (success) {
            tokenService.kickout();
        }
    }


    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return true-存在 false-不存在
     */
    public boolean checkUsername(String username) {
        var wrapper = Wrappers.query(Account.class)
                .eq("username", username);
        return super.getBaseMapper().exists(wrapper);
    }


    @Override
    public AccountInfo getAccountInfo() {
        int loginUserId = LoginUserContext.getLoginUserId();
        Account account = ChainWrappers.queryChain(getBaseMapper())
                .select("user_id", "email")
                .eq("id", loginUserId)
                .one();
        return AccountConverter.INSTANCE.toAccountInfo(account);
    }

    /**
     * 检查邮箱是否存在
     * @param email 邮箱
     * @return true-存在 false-不存在
     */
    public boolean checkEmail(String email) {
        var wrapper = Wrappers.query(Account.class)
                .eq("email", email);
        return super.getBaseMapper().exists(wrapper);
    }


    private boolean restPassword(ResetPassParam param, String type) {
        String captchaType = EmailCaptchaService.resolveCaptchaType(type);
        String email = param.getEmail();
        boolean verify = captchaService.verify(captchaType, email, param.getCaptcha());
        if (!verify) {
            throw new CaptchaServiceException("验证码无效或过期");
        }
        captchaService.deleteCaptcha(captchaType, email);
        //用户不存在
        if (!checkEmail(param.getEmail())) {
            throw new ServiceException("用户不存在");
        }
        String encryptedPassword = passwordEncoder.encode(param.getPassword());
        Boolean updated = transactionTemplate.execute(status -> {
            return ChainWrappers.updateChain(getBaseMapper())
                    .set("`password`", encryptedPassword)
                    .eq("email", email)
                    .update();
        });
        return updated != null && updated;
    }

    private void throwIfFalse(Supplier<Boolean> supplier, String error_msg) {
        if (supplier != null && Boolean.TRUE.equals(supplier.get())) {
            return;
        }
        throw new ServiceException(error_msg);
    }

}
