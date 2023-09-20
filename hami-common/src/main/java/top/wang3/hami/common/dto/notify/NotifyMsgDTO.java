package top.wang3.hami.common.dto.notify;


import lombok.Data;

import java.util.Date;

@Data
public class NotifyMsgDTO {

    private Integer id;
    private Date ctime;
    private Boolean state;
    private Info sender; //发送者信息
    private Info relatedInfo; //关联的信息
    private Info itemInfo; //元素信息
    private Info parentInfo; //关联的父信息 可为空

    public static class Info {
        private Integer id;
        private String name;
        private String image;
        private String detail;
    }
}
