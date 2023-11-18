package top.wang3.hami.core.service.mail.template;

import org.springframework.stereotype.Component;
import top.wang3.hami.common.dto.captcha.RegisterEmailCaptcha;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class RegisterMailTemplate implements MailTemplate<RegisterEmailCaptcha> {

    public static final String REGISTER_TEMPLATE = "欢迎注册Hami社区!%n您的验证码为: %s 有效期%s分钟, 请勿泄露~";


    @Override
    public List<String> getReceivers(RegisterEmailCaptcha item) {
        return List.of(item.getItem());
    }

    @Override
    public String getSubject(RegisterEmailCaptcha item) {
        return "注册验证码";
    }

    @Override
    public String render(RegisterEmailCaptcha item) {
        return REGISTER_TEMPLATE.formatted(item.getValue(),
                TimeUnit.SECONDS.toMinutes(item.getExpire()));
    }

    @Override
    public boolean html() {
        return false;
    }
}
