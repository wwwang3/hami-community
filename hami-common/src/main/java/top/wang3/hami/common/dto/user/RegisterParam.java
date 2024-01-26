package top.wang3.hami.common.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
 * 注册请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterParam {

    /**
     * 用户名
     */
    @Pattern(regexp = "^([a-zA-Z0-9_\\u4e00-\\u9fa5]{2,20})$")
    private String username;

    /**
     * 用户名
     */
    @Email
    @NotBlank
    private String email;

    /**
     * 邮箱验证码
     */
    @Length(min = 6, max = 6)
    private String captcha;

    /**
     * 密码
     */
    @Pattern(regexp = "^(?!.*\\s)(?!^[\\u4e00-\\u9fa5]+$)(?!^[0-9]+$)(?!^[A-z]+$)(?!^[^A-z0-9]+$).{8,16}$")
    private String password;
}
