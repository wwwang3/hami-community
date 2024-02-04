package top.wang3.hami.core.service.account.impl;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.converter.AccountConverter;
import top.wang3.hami.common.converter.UserConverter;
import top.wang3.hami.common.dto.captcha.CaptchaType;
import top.wang3.hami.common.dto.user.RegisterParam;
import top.wang3.hami.common.dto.user.ResetPassParam;
import top.wang3.hami.common.dto.user.UserProfileParam;
import top.wang3.hami.common.message.AccountRabbitMessage;
import top.wang3.hami.common.message.EntityMessageType;
import top.wang3.hami.common.message.UserRabbitMessage;
import top.wang3.hami.common.model.Account;
import top.wang3.hami.common.model.User;
import top.wang3.hami.common.model.UserStat;
import top.wang3.hami.common.util.Predicates;
import top.wang3.hami.common.vo.user.AccountInfo;
import top.wang3.hami.core.cache.CacheService;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.exception.CaptchaServiceException;
import top.wang3.hami.core.exception.HamiServiceException;
import top.wang3.hami.core.service.account.AccountService;
import top.wang3.hami.core.service.account.repository.AccountRepository;
import top.wang3.hami.core.service.captcha.impl.EmailCaptchaService;
import top.wang3.hami.core.service.stat.repository.UserStatRepository;
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
    private final UserStatRepository userStatRepository;
    private final RabbitMessagePublisher rabbitMessagePublisher;
    private final CacheService cacheService;

    @Resource
    PasswordEncoder passwordEncoder;

    @Resource
    TransactionTemplate transactionTemplate;

    @Resource
    TokenService tokenService;

    @Value("${hami.mode:prod}")
    private String mode;

    @Override
    public Account getAccountByEmailOrUsername(String account) {
        Integer accountId = accountRepository.getAccountId(account);
        if (accountId == null) return null;
        return getAccountById(accountId);
    }

    @Override
    public AccountInfo getAccountInfo() {
        int userId = LoginUserContext.getLoginUserId();
        Account account = getAccountById(userId);
        return AccountConverter.INSTANCE.toAccountInfo(account);
    }

    private Integer getAccountId(String account) {
        return cacheService.get(
                "account:id:" + account,
                () -> accountRepository.getAccountId(account),
                TimeoutConstants.DEFAULT_EXPIRE
        );
    }

    private Account getAccountById(Integer id) {
        String key = RedisConstants.ACCOUNT_INFO + id;
        return cacheService.get(
                key,
                () -> accountRepository.getAccountInfo(id),
                TimeoutConstants.ACCOUNT_INFO_EXPIRE
        );
    }

    @Override
    public boolean register(RegisterParam param) throws SecurityException {
        // 校验验证码
        String email = param.getEmail();
        String username = param.getUsername();
        String captcha = param.getCaptcha();
        if (!captchaService.verify(CaptchaType.REGISTER, email, captcha)) {
            throw new CaptchaServiceException("验证码无效或过期");
        }
        // 判断用户名和邮箱是否被注册过
        if (checkUsername(param.getUsername())) {
            throw new HamiServiceException("用户名已被注册");
        }
        // 验证完用户名再删除验证码
        captchaService.deleteCaptcha(CaptchaType.REGISTER, email);
        if (checkEmail(email)) {
            throw new HamiServiceException("邮箱已被注册");
        }
        // 加密密码
        String encryptedPassword = passwordEncoder.encode(param.getPassword());
        Account account = new Account(username, email, "author", encryptedPassword, Constants.ONE);
        Boolean success = transactionTemplate.execute(status -> Predicates.check(accountRepository.save(account))
                .then(() -> {
                    // 插入用户表
                    User user = new User(account.getId(), account.getUsername());
                    if (account.getId() <= 1000) {
                        user.setTag("内测用户");
                    }
                    if ("test".equals(mode)) {
                        return userRepository.updateUser(account.getId(), user);
                    }
                    return userRepository.save(user);
                }).then(() -> {
                    if ("test".equals(mode)) {
                        return true;
                    }
                    // 插入数据表, 异步插入感觉也行, 反正没几个访问量
                    UserStat stat = new UserStat();
                    stat.setUserId(account.getId());
                    return userStatRepository.save(stat);
                })
                .ifFalse(status::setRollbackOnly)
                .get()
        );
        if (Boolean.TRUE.equals(success)) {
            AccountRabbitMessage accountRabbitMessage = new AccountRabbitMessage(account.getId(), EntityMessageType.CREATE);
            UserRabbitMessage userRabbitMessage = new UserRabbitMessage(UserRabbitMessage.Type.USER_CREATE, account.getId());
            rabbitMessagePublisher.publishMsg(accountRabbitMessage);
            rabbitMessagePublisher.publishMsg(userRabbitMessage);
            return true;
        }
        return false;
    }

    @Override
    public boolean resetPassword(ResetPassParam param) {
        // 重置密码
        return resetPassword(param, CaptchaType.RESET_PASS);
    }


    @Override
    public boolean updatePassword(ResetPassParam param) {
        // 修改密码
        // 清除用户所有的登录态 (jwt+黑名单机制无法做到清除所有登录态, 因为服务器没有保存相关信息)
        boolean success = resetPassword(param, CaptchaType.UPDATE_PASS);
        if (success) {
            tokenService.kickout();
        }
        return success;
    }

    @Override
    public boolean checkUsername(String username) {
        return accountRepository.getAccountId(username) != null;
    }

    @Override
    public boolean checkEmail(String email) {
        return accountRepository.getAccountId(email) != null;
    }

    @Override
    public boolean updateProfile(UserProfileParam param) {
        User user = UserConverter.INSTANCE.toUser(param);
        // 暂不支持修改用户名
        user.setUsername(null);
        int loginUserId = LoginUserContext.getLoginUserId();
        boolean success = userRepository.updateUser(loginUserId, user);
        if (Boolean.TRUE.equals(success)) {
            UserRabbitMessage message = new UserRabbitMessage(UserRabbitMessage.Type.USER_UPDATE, loginUserId);
            rabbitMessagePublisher.publishMsg(message);
            return true;
        }
        return false;
    }

    private boolean resetPassword(ResetPassParam param, CaptchaType type) {
        final String email = param.getEmail();
        boolean verify = captchaService.verify(type, email, param.getCaptcha());
        if (!verify) {
            throw new CaptchaServiceException("验证码无效或过期");
        }
        captchaService.deleteCaptcha(type, email);
        // 用户不存在
        if (!checkEmail(param.getEmail())) {
            throw new HamiServiceException("用户不存在");
        }
        Account account = getAccountByEmailOrUsername(email);
        String old = account.getPassword();
        String encryptedPassword = passwordEncoder.encode(param.getPassword());
        boolean success = accountRepository.updatePassword(email, old, encryptedPassword);
        if (success) {
            AccountRabbitMessage accountRabbitMessage = new AccountRabbitMessage(account.getId(), EntityMessageType.UPDATE);
            rabbitMessagePublisher.publishMsg(accountRabbitMessage);
            return true;
        }
        return false;
    }

}
