package top.wang3.hami.common.dto.captcha;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ResetPassEmailCaptcha extends EmailCaptcha {


    public ResetPassEmailCaptcha(String item, String value, long expire) {
        super(item, value, expire);
    }

    public ResetPassEmailCaptcha(String item, String value) {
        super(item, value);
    }

    @Override
    public CaptchaType type() {
        return CaptchaType.RESET_PASS;
    }
}
