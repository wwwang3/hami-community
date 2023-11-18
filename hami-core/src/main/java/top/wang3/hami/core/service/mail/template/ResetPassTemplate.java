package top.wang3.hami.core.service.mail.template;

import org.springframework.stereotype.Component;
import top.wang3.hami.common.dto.captcha.ResetPassEmailCaptcha;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class ResetPassTemplate implements MailTemplate<ResetPassEmailCaptcha> {

    public static final String RESET_TEMPLATE = "你正在重置密码!%n您的验证码为: %s 有效期%s分钟, 请勿泄露, 如非本人操作请忽略!";

    @Override
    public List<String> getReceivers(ResetPassEmailCaptcha item) {
        return List.of(item.getItem());
    }

    @Override
    public String getSubject(ResetPassEmailCaptcha item) {
        return "重置密码验证码";
    }

    @Override
    public String render(ResetPassEmailCaptcha item) {
        return RESET_TEMPLATE.formatted(item.getValue(),
                TimeUnit.SECONDS.toMinutes(item.getExpire()));
    }

    @Override
    public boolean html() {
        return false;
    }
}
