package top.wang3.hami.common.dto.notify;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.dto.UserDTO;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleMsgDTO {
    private Integer id;
    private Integer articleId;
    private Integer userId;
    private String title;
    private Date ctime;
    private UserDTO user;
}
