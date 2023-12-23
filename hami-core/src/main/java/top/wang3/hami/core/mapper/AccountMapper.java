package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wang3.hami.common.model.Account;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {

    Account selectAccountByUsernameOrEmail(@Param("account") String account);
}