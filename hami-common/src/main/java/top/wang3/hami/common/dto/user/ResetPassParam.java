package top.wang3.hami.common.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 重置密码请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPassParam {

    /**
     * 邮箱
     */
    @Email
    @NotBlank
    private String email;

    /**
     * 验证码
     */
    @Pattern(regexp = "\\d{6}")
    private String captcha;

    /**
     * 新密码
     */
    @Pattern(regexp = "^(?!.*\\s)(?!^[\\u4e00-\\u9fa5]+$)(?!^[0-9]+$)(?!^[A-z]+$)(?!^[^A-z0-9]+$).{8,16}$")
    private String password;
}
