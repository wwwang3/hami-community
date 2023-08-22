package top.wang3.hami.common.dto;


import lombok.Data;

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
     * 我点赞的文章数
     */
    private long likes;

    /**
     * 我收藏的文章数
     */
    private long collects;

    /**
     * 粉丝数
     */
    private long followers;

    /**
     * 关注总数
     */
    private long followings;
}
