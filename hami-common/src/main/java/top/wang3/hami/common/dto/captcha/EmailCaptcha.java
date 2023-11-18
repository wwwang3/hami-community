package top.wang3.hami.common.dto.captcha;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.message.email.EmailRabbitMessage;
import top.wang3.hami.common.message.email.MailMessage;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class EmailCaptcha implements Captcha, MailMessage, EmailRabbitMessage {

    private String item;
    private String value;
    private long expire = DEFAULT_EXPIRE;

    public EmailCaptcha(String item, String value) {
        this.item = item;
        this.value = value;
    }

    @Override
    public String item() {
        return item;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public long expire() {
        return expire;
    }
}
