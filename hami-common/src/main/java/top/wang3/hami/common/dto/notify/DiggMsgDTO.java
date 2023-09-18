package top.wang3.hami.common.dto.notify;


import lombok.Data;
import top.wang3.hami.common.dto.UserDTO;

import java.util.Date;

@Data
public class DiggMsgDTO {

    private Integer id;
    private Integer userId;
    private Integer itemId; //文章ID
    private Integer type;
    private UserDTO user; //发送消息的用户信息
    private String title; //文章标题
    private String content; //评论内容 如果点赞的是评论
    private Date ctime;
}
