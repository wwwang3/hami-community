package top.wang3.hami.mail.supplier;

import top.wang3.hami.mail.model.MailSendResult;
import top.wang3.hami.mail.model.PrepareMimeMessageHelper;
import top.wang3.hami.mail.sender.CustomMailSender;


public interface MailSenderSupplier {

    CustomMailSender getMailSender();

    CustomMailSender getMailSender(String key);

    MailSendResult doRetry(MailSendResult result, String failedSender);

    void setRetry(boolean retry);

    MailSendResult send(PrepareMimeMessageHelper helper);
}
