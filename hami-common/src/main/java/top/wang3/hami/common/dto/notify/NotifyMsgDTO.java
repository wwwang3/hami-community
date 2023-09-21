package top.wang3.hami.common.dto.notify;


import lombok.Data;

import java.util.Date;

@Data
public class NotifyMsgDTO {

    private Integer id;
    private Date ctime;
    private byte state;
    private byte type;
    private Info sender; //发送者信息
    private Info relatedInfo; //关联的信息 一般为文章
    private Info itemInfo; //元素信息


}
