package top.wang3.hami.common.model;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户账号表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "account")
@Builder
public class Account {
    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户名
     */
    @TableField(value = "username")
    private String username;

    /**
     * 邮箱
     */
    @TableField(value = "email")
    private String email;

    /**
     * 角色
     */
    @TableField(value = "`role`")
    private String role;

    /**
     * 密码
     */
    @TableField(value = "`password`")
    private String password;

    /**
     * 状态 0-未激活 1-激活
     */
    @TableField(value = "`state`")
    private Byte state;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @TableField(value = "deleted")
    @TableLogic
    @JsonIgnore
    private Byte deleted;

    /**
     * 修改时间
     */
    @TableField(value = "ctime")
    private Date ctime;

    /**
     * 更新时间
     */
    @TableField(value = "mtime")
    private Date mtime;

    public Account(String username, String email, String role, String password, Byte state) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.password = password;
        this.state = state;
    }
}