package top.wang3.hami.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterParam {

    @Pattern(regexp = "^([a-zA-Z0-9_\\u4e00-\\u9fa5]{2,16})$")
    private String username;

    @Email
    @NotEmpty
    private String email;

    @Length(min = 6, max = 6)
    private String captcha;

    @Pattern(regexp = "^(?!.*\\s)(?!^[\\u4e00-\\u9fa5]+$)(?!^[0-9]+$)(?!^[A-z]+$)(?!^[^A-z0-9]+$).{8,16}$")
    private String password;
}
