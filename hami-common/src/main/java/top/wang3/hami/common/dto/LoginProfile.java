package top.wang3.hami.common.dto;


import lombok.Data;

import java.util.Date;

/**
 * 进入首页时获取, 以此判断是否登录
 */
@Data
public class LoginProfile {

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
     * 个人简介
     */
    private String profile;

    /**
     * 标签
     */
    private String tag;

    /**
     * 加入时间
     */
    private Date ctime;

    /**
     * 我点赞的文章数
     */
    private long likes;

    /**
     * 我收藏的文章数
     */
    private long collects;

    /**
     * 我的粉丝数
     */
    private long followers;

    /**
     * 我的关注数
     */
    private long followings;
}
