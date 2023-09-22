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
 * 文章标签
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "article_tag")
public class ArticleTag {
    /**
     * 标签ID,主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 文章ID
     */
    @TableField(value = "article_id")
    private Integer articleId;

    /**
     * 标签ID
     */
    @TableField(value = "tag_id")
    private Integer tagId;

    /**
     * 更新时间
     */
    @TableField(value = "ctime")
    private Date ctime;

    /**
     * 创建时间
     */
    @TableField(value = "mtime")
    private Date mtime;


    public ArticleTag(Integer articleId, Integer tagId) {
        this.articleId = articleId;
        this.tagId = tagId;
    }
}