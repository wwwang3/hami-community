package top.wang3.hami.mail.manager;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import top.wang3.hami.mail.model.MailSendResult;
import top.wang3.hami.mail.model.PrepareMimeMessageHelper;
import top.wang3.hami.mail.sender.CustomMailSender;

import java.util.List;

@Getter
@Slf4j
public abstract class AbstractMailSenderManager implements MailSenderManager {

    private final List<CustomMailSender> senders;

    private boolean retry;

    public AbstractMailSenderManager(List<CustomMailSender> senders) {
        if (senders == null || senders.isEmpty())
            throw new IllegalStateException("mail-senders can not be null or empty");
        this.senders = senders;
    }

    public int getSize() {
        return senders.size();
    }

    @Override
    public void setRetry(boolean retry) {
        this.retry = retry;
    }

    @Override
    public CustomMailSender getMailSender(String key) {
        for (CustomMailSender sender : senders) {
            if (sender.getKey().equals(key)) return sender;
        }
        throw new IllegalArgumentException("no such MailSender of this key");
    }

    @Override
    public MailSendResult send(PrepareMimeMessageHelper helper) {
        var service = helper.getCustomMailSender();
        var messages = helper.build();
        MailSendResult result = service.send(messages);
        MimeMessage[] failedMessages = result.getFailedMessages();
        if (failedMessages != null && failedMessages.length > 0) {
            return doRetry(result, helper.getCustomMailSender().getKey());
        }
        return result;
    }

    @Override
    public MailSendResult doRetry(MailSendResult result, String failedSender) {
        if (!retry) return result;
        log.info("start to retry send mail");
        int failedCount = 1;
        var finalResult = result;
        var messages = result.getFailedMessages();
        for (CustomMailSender sender : senders) {
            if (failedSender.equals(sender.getKey())) {
                continue;
            }
            log.info("current sender is {}", sender.getKey());
            resetFrom(messages, sender.getJavaMailSender().getUsername());
            finalResult = sender.send(messages);
            var againFailed = finalResult.getFailedMessages();
            if (againFailed != null && againFailed.length > 0) {
                log.warn("current sender send the following mail failed: {}, msg: {}", againFailed,
                        finalResult.getMsg());
                failedCount++;
                messages = againFailed;
            } else {
                //发送失败的消息为空
                break;
            }
        }
        if (failedCount == getSize()) {
            //所有的MailSender都试过
            log.error("Tried all MailSenders, still some emails are not sent successfully, error_msg: {}",
                    finalResult.getMsg());
        } else {
            log.info("retry success");
        }
        return finalResult;
    }

    private void resetFrom(MimeMessage[] mimeMessages, String from) {
        for (MimeMessage mimeMessage : mimeMessages) {
            try {
                mimeMessage.setFrom(from);
            } catch (MessagingException e) {
                log.debug("set from failed: {}", e.getMessage());
            }
        }
    }
}
