package top.wang3.hami.common.vo.user;


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
    private Integer userId;

    /**
     * 账户名
     */
    private String account;

    /**
     * 用户名(昵称)
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
     * 修改时间
     */
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
