package top.wang3.hami.common.vo.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.dto.IpInfo;
import top.wang3.hami.common.vo.user.UserVo;

import java.util.Date;

/**
 * 评论
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentVo {

    /**
     * 评论ID
     */
    private Integer id;

    /**
     * 文章ID
     */
    private Integer articleId;

    /**
     * 发表评论的用户ID
     */
    private Integer userId;

    /**
     * 评论发表时的IP信息
     */
    private IpInfo ipInfo;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论图片
     */
    private String contentImg;

    /**
     * 根评论ID
     */
    private Integer rootId;

    /**
     * 父评论ID
     */
    private Integer parentId;

    /**
     * 评论点赞数
     */
    private Integer likes;

    /**
     * 创建时间
     */
    private Date ctime;

    /**
     * 修改时间
     */
    private Date mtime;

    /**
     * 回复的用户ID
     */
    private Integer replyTo;

    /**
     * 发表评论的用户信息
     */
    private UserVo user;

    /**
     * 回复用户信息
     */
    private UserVo replyUser;

    /**
     * 子评论
     */
    private ReplyVo reply;

    /**
     * 是否点赞
     */
    private Boolean liked = false;

}
