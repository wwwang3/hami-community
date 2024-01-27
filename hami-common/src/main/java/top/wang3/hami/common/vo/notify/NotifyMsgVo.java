package top.wang3.hami.common.vo.notify;


import lombok.Data;
import top.wang3.hami.common.dto.notify.Info;

import java.util.Date;

/**
 * 通知消息
 */
@Data
public class NotifyMsgVo {

    /**
     * 通知消息ID
     */
    private Integer id;

    /**
     * 通知创建时间
     */
    private Date ctime;

    /**
     * 通知状态 0-未读 1-已读
     */
    private byte state;

    /**
     * 通知类型
     * @see top.wang3.hami.common.dto.notify.NotifyType
     */
    private byte type;

    /**
     * 发送者信息
     */
    private Info sender;

    /**
     * 该通知关联的信息, 一般为触发消息通知的元素的信息, 如文章信息, 评论信息
     */
    private Info relatedInfo;

    /**
     * 通知内容
     */
    private Info itemInfo;

}
