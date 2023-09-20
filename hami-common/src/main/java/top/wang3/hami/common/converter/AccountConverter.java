package top.wang3.hami.common.converter;


import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import top.wang3.hami.common.dto.user.AccountInfo;
import top.wang3.hami.common.model.Account;

@Mapper
public interface AccountConverter {

    AccountConverter INSTANCE = Mappers.getMapper(AccountConverter.class);

    AccountInfo toAccountInfo(Account account);
}
