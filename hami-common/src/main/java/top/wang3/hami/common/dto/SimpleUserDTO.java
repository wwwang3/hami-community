package top.wang3.hami.common.dto;


import lombok.Data;

@Data
public class SimpleUserDTO {

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
     * 粉丝数
     */
    private Long followers;

    /**
     * 关注数
     */
    private Long followings;

    /**
     * 当前登录用户是否关注此用户
     */
    private boolean followed;
}
