package top.wang3.hami.common.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
    * 点赞通用表
    */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "tb_like")
public class LikeItem {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 实体ID -文章/评论
     */
    @TableField(value = "item_id")
    private Integer itemId;

    /**
     * 实体类型 1-文章 2-评论
     */
    @TableField(value = "item_type")
    private Byte itemType;

    /**
     * 点赞人ID
     */
    @TableField(value = "liker_id")
    private Integer likerId;

    /**
     * 状态 0-未点赞 1-点赞
     */
    @TableField(value = "`state`")
    private Byte state;

    /**
     * 创建时间
     */
    @TableField(value = "ctime")
    private Date ctime;

    /**
     * 更新时间
     */
    @TableField(value = "mtime")
    private Date mtime;

    public LikeItem(Integer itemId, Byte itemType, Integer likerId) {
        this.itemId = itemId;
        this.itemType = itemType;
        this.likerId = likerId;
    }
}