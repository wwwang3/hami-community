package top.wang3.hami.common.dto.captcha;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RegisterEmailCaptcha extends EmailCaptcha {


    public RegisterEmailCaptcha(String item, String value) {
        super(item, value);
    }

    public RegisterEmailCaptcha(String item, String value, long expire) {
        super(item, value, expire);
    }

    @Override
    public CaptchaType type() {
        return CaptchaType.REGISTER;
    }
}
