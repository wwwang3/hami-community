package top.wang3.hami.common.vo.user;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import top.wang3.hami.common.dto.stat.UserStatDTO;

import java.util.Date;

/**
 * 当前登录用户信息
 */
@Data
public class LoginProfile {

    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    private Integer userId;

    /**
     * 用户名
     */
    @JsonProperty("username")
    private String username;

    /**
     * 头像
     */
    @JsonProperty("avatar")
    private String avatar;

    /**
     * 职位
     */
    @JsonProperty("position")
    private String position;

    /**
     * 公司
     */
    @JsonProperty("company")
    private String company;

    /**
     * 个人简介
     */
    @JsonProperty("profile")
    private String profile;

    /**
     * 个人主页
     */
    @JsonProperty("blog")
    private String blog;

    /**
     * 标签
     */
    @JsonProperty("tag")
    private String tag;
    /**
     * 创建时间
     */
    @JsonProperty("ctime")
    private Date ctime;

    /**
     * 修改时间
     */
    @JsonProperty("mtime")
    private Date mtime;

    /**
     * 我点赞的文章数
     */
    private Integer likes = 0;

    /**
     * 我收藏的文章数
     */
    private Integer collects = 0;

    /**
     * 我的粉丝数
     */
    private Integer followers = 0;

    /**
     * 我的关注数
     */
    private Integer followings = 0;

    /**
     * 数据
     */
    private UserStatDTO stat;

}
