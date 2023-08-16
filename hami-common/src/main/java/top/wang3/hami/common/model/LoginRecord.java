package top.wang3.hami.common.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.dto.IpInfo;

import java.util.Date;

/**
    * 登录记录表
    */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "login_record")
public class LoginRecord {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Integer userId;

    /**
     * 登录的IP地址信息
     */
    @TableField(value = "ip_info", typeHandler = JacksonTypeHandler.class)
    private IpInfo ipInfo;

    /**
     * 登录时间
     */
    @TableField(value = "login_time")
    private Date loginTime;

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
     * 更新时间
     */
    @TableField(value = "mtime")
    private Date mtime;
}