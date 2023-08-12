package top.wang3.hami.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.model.Account;
import top.wang3.hami.core.mapper.AccountMapper;
import top.wang3.hami.core.service.AccountService;

@Service
@Slf4j
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account>
        implements AccountService {

    @Override
    public Account getAccountByEmailOrUsername(String account) {
        var wrapper = Wrappers.lambdaQuery(Account.class)
                .eq(Account::getUsername, account)
                .or()
                .eq(Account::getEmail, account);
        return super.getOne(wrapper);
    }
}
