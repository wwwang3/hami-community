package top.wang3.hami.common.dto.notify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.dto.UserDTO;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentMsgDTO {
    private Integer id;
    private Integer articleId;
    private Integer commentId;
    private Integer userId;
    private UserDTO user;
    private String content;
    private String title;
    private Integer type;
    private Date ctime;
}
