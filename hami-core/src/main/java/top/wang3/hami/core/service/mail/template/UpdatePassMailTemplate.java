package top.wang3.hami.core.service.mail.template;

import org.springframework.stereotype.Component;
import top.wang3.hami.common.dto.captcha.UpdatePassEmailCaptcha;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static top.wang3.hami.core.service.mail.template.ResetPassTemplate.RESET_TEMPLATE;

@Component
public class UpdatePassMailTemplate implements MailTemplate<UpdatePassEmailCaptcha> {

    @Override
    public List<String> getReceivers(UpdatePassEmailCaptcha item) {
        return List.of(item.getItem());
    }

    @Override
    public String getSubject(UpdatePassEmailCaptcha item) {
        return "修改密码验证码";
    }

    @Override
    public String render(UpdatePassEmailCaptcha item) {
        return RESET_TEMPLATE.formatted(item.getValue(),
                TimeUnit.SECONDS.toMinutes(item.getExpire()));
    }

    @Override
    public boolean html() {
        return false;
    }
}
