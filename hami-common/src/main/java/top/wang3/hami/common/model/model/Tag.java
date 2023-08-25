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
 * 标签管理表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "tag")
public class Tag {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 标签名称
     */
    @TableField(value = "`name`")
    private String name;

    /**
     * 标签类型：1-系统标签，2-自定义标签
     */
    @TableField(value = "`type`")
    private Integer type;

    /**
     * 类目ID
     */
    @TableField(value = "category_id")
    private Integer categoryId;

    /**
     * 是否删除
     */
    @TableField(value = "deleted")
    private Byte deleted;

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