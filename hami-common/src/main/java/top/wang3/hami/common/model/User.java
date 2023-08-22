package top.wang3.hami.common.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户信息表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "`user`")
public class User {

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "user_id")
    private Integer userId;

    /**
     * 用户名
     */
    @TableField(value = "username")
    private String username;

    /**
     * 头像
     */
    @TableField(value = "avatar")
    private String avatar;

    /**
     * 职位
     */
    @TableField(value = "`position`")
    private String position;

    /**
     * 公司
     */
    @TableField(value = "company")
    private String company;

    /**
     * 个人简介
     */
    @TableField(value = "profile")
    private String profile;

    /**
     * 个人主页
     */
    @TableField(value = "blog")
    private String blog;

    /**
     * 标签
     */
    @TableField(value = "tag")
    private String tag;

    /**
     * 是否删除 0-删除 1-未删除
     */
    @TableField(value = "deleted")
    private Byte deleted;

    /**
     * 创建时间
     */
    @TableField(value = "ctime")
    private Date ctime;

    /**
     * 更新时间
     */
    @TableField(value = "mtime")
    private Date mtime;

    public User(Integer userId, String username) {
        this.userId = userId;
        this.username = username;
    }
}