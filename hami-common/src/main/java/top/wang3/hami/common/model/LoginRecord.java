package top.wang3.hami.common.model;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.dto.IpInfo;

import java.util.Date;

/**
* 登录记录
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "login_record", autoResultMap = true)
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
    @JsonProperty("user_id")
    private Integer userId;

    /**
     * 登录的IP地址信息
     */
    @TableField(value = "ip_info", typeHandler = JacksonTypeHandler.class)
    @JsonProperty("ip_info")
    private IpInfo ipInfo;

    @TableField(value = "deleted")
    @TableLogic
    @JsonIgnore
    private Byte deleted;

    /**
     * 登录时间
     */
    @TableField(value = "login_time")
    @JsonProperty("login_time")
    private Date loginTime;

}