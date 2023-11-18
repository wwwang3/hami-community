package top.wang3.hami.core.service.account.impl;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import top.wang3.hami.common.converter.AccountConverter;
import top.wang3.hami.common.converter.UserConverter;
import top.wang3.hami.common.dto.captcha.CaptchaType;
import top.wang3.hami.common.dto.user.AccountInfo;
import top.wang3.hami.common.dto.user.RegisterParam;
import top.wang3.hami.common.dto.user.ResetPassParam;
import top.wang3.hami.common.dto.user.UserProfileParam;
import top.wang3.hami.common.message.UserRabbitMessage;
import top.wang3.hami.common.model.Account;
import top.wang3.hami.common.model.User;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.exception.CaptchaServiceException;
import top.wang3.hami.core.exception.HamiServiceException;
import top.wang3.hami.core.service.account.AccountService;
import top.wang3.hami.core.service.account.repository.AccountRepository;
import top.wang3.hami.core.service.captcha.impl.EmailCaptchaService;
import top.wang3.hami.core.service.user.repository.UserRepository;
import top.wang3.hami.security.context.LoginUserContext;
import top.wang3.hami.security.service.TokenService;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final EmailCaptchaService captchaService;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final RabbitMessagePublisher rabbitMessagePublisher;

    @Resource
    PasswordEncoder passwordEncoder;

    @Resource
    TransactionTemplate transactionTemplate;

    @Resource
    TokenService tokenService;

    @Override
    public Account getAccountByEmailOrUsername(String account) {
        return accountRepository.getAccountByEmailOrUsername(account);
    }

    @Override
    public boolean register(RegisterParam param) throws SecurityException {
        //校验验证码
        String email = param.getEmail();
        String username = param.getUsername();
        String captcha = param.getCaptcha();
        if (!captchaService.verify(CaptchaType.REGISTER, email, captcha)) {
            throw new CaptchaServiceException("验证码无效或过期");
        }
        //判断用户名和邮箱是否被注册过
        if (checkUsername(param.getUsername())) {
            throw new HamiServiceException("用户名已被注册");
        }
        //验证完用户名再删除验证码
        captchaService.deleteCaptcha(CaptchaType.REGISTER, email);
        if (checkEmail(email)) {
            throw new HamiServiceException("邮箱已被注册");
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
        Boolean success = transactionTemplate.execute(status -> {
            boolean saved1 = accountRepository.save(account);
            if (!saved1) return false;
            User user = new User(account.getId(), username);
            if (account.getId() < 1000) {
                user.setTag("内测用户");
            }
            if (!userRepository.save(user)) {
                status.setRollbackOnly();
            }
            return true;
        });
        if (Boolean.TRUE.equals(success)) {
            UserRabbitMessage message = new UserRabbitMessage(UserRabbitMessage.Type.USER_CREATE, account.getId());
            rabbitMessagePublisher.publishMsg(message);
            return true;
        }
        return false;
    }

    @Override
    public boolean resetPassword(ResetPassParam param) {
        //校验验证码
        return restPassword(param, CaptchaType.RESET_PASS);
    }


    @Override
    public boolean updatePassword(ResetPassParam param) {
        //修改密码
        //清除用户所有的登录态
        boolean success = restPassword(param, CaptchaType.UPDATE_PASS);
        if (success) {
            tokenService.kickout();
        }
        return success;
    }

    @Override
    public boolean checkUsername(String username) {
        return accountRepository.checkUsername(username);
    }

    @Override
    public boolean checkEmail(String email) {
        return accountRepository.checkEmail(email);
    }

    @Override
    public boolean updateProfile(UserProfileParam param) {
        User user = UserConverter.INSTANCE.toUser(param);
        //暂不支持修改用户名
        user.setUsername(null);
        int loginUserId = LoginUserContext.getLoginUserId();

        Boolean success = transactionTemplate.execute(status -> {
            //暂不支持修改
            //更新账号信息
//            if (saved && StringUtils.hasText(username)) {
//                Account account = new Account();
//                account.setId(loginUserId);
//                account.setUsername(username);
//                return accountRepository.updateById(account);
//            }
            return userRepository.updateUser(loginUserId, user);
        });
        if (Boolean.TRUE.equals(success)) {
            UserRabbitMessage message = new UserRabbitMessage(UserRabbitMessage.Type.USER_UPDATE, loginUserId);
            rabbitMessagePublisher.publishMsg(message);
            return true;
        }
        return false;
    }

    @Override
    public AccountInfo getAccountInfo() {
        int userId = LoginUserContext.getLoginUserId();
        Account info = accountRepository.getAccountInfo(userId);
        return AccountConverter.INSTANCE.toAccountInfo(info);
    }


    private boolean restPassword(ResetPassParam param, CaptchaType type) {
        final String email = param.getEmail();
        boolean verify = captchaService.verify(type, email, param.getCaptcha());
        if (!verify) {
            throw new CaptchaServiceException("验证码无效或过期");
        }
        captchaService.deleteCaptcha(type, email);
        //用户不存在
        if (!checkEmail(param.getEmail())) {
            throw new HamiServiceException("用户不存在");
        }
        Account account = getAccountByEmailOrUsername(email);
        final String old = account.getPassword();
        final String encryptedPassword = passwordEncoder.encode(param.getPassword());
        Boolean updated = transactionTemplate.execute(status -> {
            return accountRepository.updatePassword(email, old, encryptedPassword);
        });
        return Boolean.TRUE.equals(updated);
    }

}
