package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wang3.hami.common.model.Account;

import java.util.List;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {

    Account selectAccountByUsernameOrEmail(@Param("account") String account);

    Integer selectAccountId(@Param("account") String account);

    List<Account> scanAccountDesc(@Param("maxId") int maxId, @Param("batchSize") int batchSize);
}