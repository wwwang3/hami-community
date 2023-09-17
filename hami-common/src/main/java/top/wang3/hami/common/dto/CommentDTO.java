package top.wang3.hami.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.model.Comment;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {

    private Integer id;
    private Integer articleId;
    private Integer userId;

    private Comment comment;

    private UserDTO user;

    private UserDTO replyTo;

    private ReplyDTO reply;

    private Boolean liked = false;

}
