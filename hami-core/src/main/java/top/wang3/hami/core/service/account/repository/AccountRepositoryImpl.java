package top.wang3.hami.core.service.account.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Repository;
import top.wang3.hami.common.model.Account;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.mapper.AccountMapper;


@Repository
public class AccountRepositoryImpl extends ServiceImpl<AccountMapper, Account>
        implements AccountRepository {

    @Override
    public Account getAccountByEmailOrUsername(String account) {
       return getBaseMapper()
               .selectAccountByUsernameOrEmail(account);
    }

    @Override
    @CostLog
    public Integer getAccountId(String account) {
        return getBaseMapper().selectAccountId(account);
    }

    @Override
    public Account getAccountInfo(Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .eq("id", userId)
                .one();
    }

    @Override
    public boolean updatePassword(String email, String oldPassword, String newPassword) {
        return ChainWrappers.updateChain(getBaseMapper())
                .set("password", newPassword)
                .eq("email", email)
                .eq("password", oldPassword)
                .update();
    }


    @Override
    public boolean checkUsername(String username) {
        return ChainWrappers.queryChain(getBaseMapper())
                .eq("username", username)
                .exists();
    }

    @Override
    public boolean checkEmail(String email) {
        var wrapper = Wrappers.query(Account.class)
                .eq("email", email);
        return super.getBaseMapper().exists(wrapper);
    }
}
