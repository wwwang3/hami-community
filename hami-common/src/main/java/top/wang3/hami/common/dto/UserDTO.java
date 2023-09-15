package top.wang3.hami.common.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UserDTO {

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 职位
     */
    private String position;

    /**
     * 公司
     */
    private String company;

    /**
     * 个人简介
     */
    private String profile;

    /**
     * 个人主页
     */
    private String blog;

    /**
     * 标签
     */
    private String tag;

    /**
     * 创建时间
     */
    private Date ctime;

    /**
     * 用户数据
     */
    private UserStat stat;

    /**
     * 是否关注
     */
    private  boolean followed;

}
