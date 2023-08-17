package top.wang3.hami.mail.model;

import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class MailSendResult {
    private String sender;
    private String msg;
    private MimeMessage[] failedMessages;

    public static MailSendResult success(String sender) {
        return new MailSendResult(sender,"success", null);
    }

    public static MailSendResult failed(String sender, String msg, MimeMessage[] failedMessages) {
        return new MailSendResult(sender, msg, failedMessages);
    }
}
