package top.wang3.hami.mail.sender;

import jakarta.mail.internet.MimeMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import top.wang3.hami.mail.model.MailSendResult;

import java.util.Map;


@Getter
@Slf4j
public class CustomMailSender {

    private final String key;
    private final JavaMailSenderImpl javaMailSender;

    public CustomMailSender(String key, JavaMailSenderImpl javaMailSender) {
        //501 Mail from address must be same as authorization user
        this.key = key;
        this.javaMailSender = javaMailSender;
    }

    public MimeMessage createMimeMessage() {
        return javaMailSender.createMimeMessage();
    }

    public MailSendResult send(MimeMessage[] mimeMessages) {
        try {
            javaMailSender.send(mimeMessages);
            log.info("current mail-sender [{}] send mail success", key);
            return MailSendResult.success(key);
        } catch (MailSendException e) {
            log.debug("send mail failed: error_class: {}, if set retry, will retry",
                    e.getClass().getSimpleName());
            return MailSendResult.failed(key, e.getMessage(), getFailedMimeMessage(e));
        } catch (Exception e) {
            //其他错误不重试
            log.debug("send mail failed: error_class: {}, error_msg: {}",
                    e.getClass().getSimpleName(), e.getMessage());
            return MailSendResult.failed(key, e.getMessage(), null);
        }
    }

    public MailSendResult send(MimeMessage message) {
        return send(new MimeMessage[]{ message });
    }

    private MimeMessage[] getFailedMimeMessage(MailSendException e) {
        Map<Object, Exception> messages = e.getFailedMessages();
        var failedMessages = new MimeMessage[messages.size()];
        int i = 0;
        for (Object o : messages.keySet()) {
            failedMessages[i++] = (MimeMessage) o;
        }
        return failedMessages;
    }
}
