package top.wang3.hami.common.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 用户数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStat {

    private int userId;

    /**
     * 文章数
     */
    private Integer totalArticles = 0;

    /**
     * 文章总阅读量
     */
    private Integer totalViews = 0;

    /**
     * 我收藏的文章数
     */
    private Integer totalCollects = 0;

    /**
     * 文章获得的总点赞数
     */
    private Integer totalLikes = 0;

    /**
     * 总评论数
     */
    private Integer totalComments = 0;

    /**
     * 粉丝数
     */
    private Integer totalFollowers = 0;

    /**
     * 关注总数
     */
    private Integer totalFollowings = 0;

    public UserStat(int userId) {
        this.userId = userId;
    }
}
