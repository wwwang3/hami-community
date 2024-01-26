package top.wang3.hami.common.vo.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("id")
    private Integer id;

    /**
     * 文章ID
     */
    @JsonProperty("article_id")
    private Integer articleId;

    /**
     * 发表评论的用户ID
     */
    @JsonProperty("user_id")
    private Integer userId;

    /**
     * 评论发表时的IP信息
     */
    @JsonProperty("ip_info")
    private IpInfo ipInfo;

    /**
     * 评论内容
     */
    @JsonProperty("content")
    private String content;

    /**
     * 评论图片
     */
    @JsonProperty("content_img")
    private String contentImg;

    /**
     * 根评论ID
     */
    @JsonProperty("root_id")
    private Integer rootId;

    /**
     * 父评论ID
     */
    @JsonProperty("parent_id")
    private Integer parentId;

    /**
     * 评论点赞数
     */
    @JsonProperty("likes")
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
    @JsonProperty("reply_to")
    private Integer replyTo;

    /**
     * 发表评论的用户信息
     */
    @JsonProperty("user")
    private UserVo user;

    /**
     * 回复用户信息
     */
    @JsonProperty("reply_user")
    private UserVo replyUser;

    /**
     * 子评论
     */
    @JsonProperty("reply")
    private ReplyVo reply;

    /**
     * 是否点赞
     */
    @JsonProperty("liked")
    private Boolean liked = false;

}
