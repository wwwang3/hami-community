package top.wang3.hami.common.dto.notify;


import lombok.Data;
import top.wang3.hami.common.dto.UserDTO;

import java.util.Date;

@Data
public class FollowMsgDTO {

    private Integer id;
    private Integer userId;
    private UserDTO user;
    private Date ctime;
}
