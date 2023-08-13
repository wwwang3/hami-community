package top.wang3.hami.core.service.account.impl;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.model.Account;
import top.wang3.hami.core.service.account.AccountService;
import top.wang3.hami.security.model.LoginUser;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AccountService accountService;

    public UserDetailsServiceImpl(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public LoginUser loadUserByUsername(String username) {
        Account account = accountService.getAccountByEmailOrUsername(username);
        if (account == null) throw new UsernameNotFoundException("用户名或密码错误");
        return LoginUser
                .withId(account.getId())
                .username(account.getUsername())
                .password(account.getPassword())
                .email(account.getEmail())
                .roles(account.getRole())
                .build();
    }


}
