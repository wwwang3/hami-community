package top.wang3.hami.common.vo.user;

import lombok.Data;

/**
 * 账户信息
 */
@Data
public class AccountInfo {

    /**
     * 账号ID
     */
    private Integer id;

    /**
     * 账户名
     */
    private String account;

    /**
     * 邮箱
     */
    private String email;

}
