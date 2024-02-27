package top.wang3.hami.common.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "bulletin")
public class Bulletin {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 内容
     */
    @TableField(value = "content")
    private String content;

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

    /**
     * 是否删除
     */
    @TableField(value = "deleted")
    @TableLogic
    private Byte deleted;
}