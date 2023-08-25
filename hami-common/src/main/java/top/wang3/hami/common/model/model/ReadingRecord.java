package top.wang3.hami.common.model.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 阅读记录表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "reading_record")
public class ReadingRecord {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 作者ID
     */
    @TableField(value = "user_id")
    private Integer userId;

    /**
     * 文章ID
     */
    @TableField(value = "article_id")
    private Integer articleId;

    /**
     * 状态 0-未读 1-已读
     */
    @TableField(value = "`state`")
    private Byte state;

    /**
     * 阅读时间
     */
    @TableField(value = "reading_time")
    private Date readingTime;

    /**
     * 是否删除
     */
    @TableField(value = "deleted")
    private Integer deleted;

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
}