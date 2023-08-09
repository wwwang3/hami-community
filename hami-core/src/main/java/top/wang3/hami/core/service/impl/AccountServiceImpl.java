package top.wang3.hami.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.model.Account;
import top.wang3.hami.core.mapper.AccountMapper;
import top.wang3.hami.core.service.AccountService;
import top.wang3.hami.security.model.LoginUser;

@Service
@Slf4j
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account>
        implements AccountService, UserDetailsService {


    @Override
    public LoginUser loadUserByUsername(String username) {
        Account account = this.getUserByUsername(username);
        if (account == null) throw new UsernameNotFoundException("用户名或密码错误");
        return LoginUser
                .withId(account.getId())
                .username(account.getUsername())
                .password(account.getPassword())
                .email(account.getEmail())
                .roles(account.getRole())
                .build();

    }

    @Override
    public Account getUserByUsername(String username) {
        var wrapper = Wrappers.lambdaQuery(Account.class)
                .eq(Account::getUsername, username);
        return super.getOne(wrapper);
    }
}
