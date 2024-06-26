package top.wang3.hami.common.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 分类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "category")
public class Category {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 类目名称
     */
    @TableField(value = "`name`")
    private String name;

    /**
     * 路径
     */
    @TableField(value = "`path`")
    private String path;

    /**
     * 是否删除
     */
    @TableField(value = "deleted")
    @JsonIgnore
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(value = "ctime")
    @JsonIgnore
    private Date ctime;

    /**
     * 最后更新时间
     */
    @TableField(value = "mtime")
    @JsonIgnore
    private Date mtime;
}