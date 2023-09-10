package top.wang3.hami.common.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 消息通知列表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName(value = "notify_msg")
public class NotifyMsg {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 备用
     */
    @TableField(value = "item_id")
    private Integer itemId;

    /**
     * 对应的实体名称
     */
    @TableField(value = "item_name")
    private String itemName;

    /**
     * 关联的主键
     */
    @TableField(value = "related_id")
    private Integer relatedId;

    /**
     * 源用户ID(发送通知)
     */
    @TableField(value = "sender")
    private Integer sender;

    /**
     * 目标用户ID(接收通知)
     */
    @TableField(value = "receiver")
    private Integer receiver;

    /**
     * 消息内容
     */
    @TableField(value = "detail")
    private String detail;

    /**
     * 类型: 0-系统，1-评论，2-回复 3-点赞 4-收藏 5-关注
     */
    @TableField(value = "`type`")
    private Integer type;

    /**
     * 阅读状态: 0-未读，1-已读
     */
    @TableField(value = "`state`")
    private Byte state;

    /**
     * 创建时间
     */
    @TableField(value = "ctime")
    private Date ctime;

    /**
     * 最后更新时间
     */
    @TableField(value = "mtime")
    private Date mtime;
}