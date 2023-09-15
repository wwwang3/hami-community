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
     * 阅读时间
     */
    @TableField(value = "reading_time", update = "now(3)")
    private Date readingTime;


    public ReadingRecord(Integer userId, Integer articleId) {
        this.userId = userId;
        this.articleId = articleId;
    }
}