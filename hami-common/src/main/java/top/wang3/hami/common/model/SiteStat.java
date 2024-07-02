package top.wang3.hami.common.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "tb_site_stat")
public class SiteStat {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "pv")
    private Integer pv;

    @TableField(value = "uv")
    private Integer uv;

    @TableField(value = "users", exist = false)
    private Integer users;

    @TableField(value = "articles", exist = false)
    private Integer articles;

    @TableField(value = "views", exist = false)
    private Integer views;

    @TableField(value ="likes", exist = false)
    private Integer likes;

    @TableField(value = "comments", exist = false)
    private Integer comments;

    @TableField(value = "collects", exist = false)
    private Integer collects;
}