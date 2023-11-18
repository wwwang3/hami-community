package top.wang3.hami.common.dto.captcha;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UpdatePassEmailCaptcha extends EmailCaptcha {


    public UpdatePassEmailCaptcha(String item, String value, long expire) {
        super(item, value, expire);
    }

    public UpdatePassEmailCaptcha(String item, String value) {
        super(item, value);
    }

    @Override
    public CaptchaType type() {
        return CaptchaType.UPDATE_PASS;
    }
}
