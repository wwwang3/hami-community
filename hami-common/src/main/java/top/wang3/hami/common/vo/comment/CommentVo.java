package top.wang3.hami.common.vo.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.dto.IpInfo;
import top.wang3.hami.common.vo.user.UserVo;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentVo {

    private Integer id;
    private Integer articleId;
    private Integer userId;
    private IpInfo ipInfo;
    private String content;
    private String contentImg;
    private Integer rootId;
    private Integer parentId;
    private Integer likes;
    private Byte deleted;
    private Date ctime;
    private Date mtime;
    private Integer replyTo;


    private UserVo user;
    private UserVo replyUser;
    private ReplyVo reply;
    private Boolean liked = false;

}
