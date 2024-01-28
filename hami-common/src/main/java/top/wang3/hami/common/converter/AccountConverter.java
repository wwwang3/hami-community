package top.wang3.hami.common.converter;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import top.wang3.hami.common.model.Account;
import top.wang3.hami.common.vo.user.AccountInfo;

@Mapper
public interface AccountConverter {

    AccountConverter INSTANCE = Mappers.getMapper(AccountConverter.class);

    @Mapping(target = "account", source = "username")
    AccountInfo toAccountInfo(Account account);
}
