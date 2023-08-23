package top.wang3.hami.common.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserProfileParam {

    /**
     * 用户名
     */
    @Pattern(regexp = "^([a-zA-Z0-9_\\u4e00-\\u9fa5]{2,20})$")
    private String username;

    /**
     * 职位
     */
    private String position;

    /**
     * 公司
     */
    private String company;

    /**
     * 个人简介
     */
    private String profile;

    /**
     * 个人主页
     */
    private String blog;

}
