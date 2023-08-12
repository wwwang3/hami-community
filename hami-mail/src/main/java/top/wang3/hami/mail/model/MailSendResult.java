package top.wang3.hami.mail.model;

import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class MailSendResult {
    private String msg;
    private MimeMessage[] failedMessages;

    public static MailSendResult success() {
        return new MailSendResult("success", null);
    }

    public static MailSendResult failed(String msg, MimeMessage[] failedMessages) {
        return new MailSendResult(msg, failedMessages);
    }
}
