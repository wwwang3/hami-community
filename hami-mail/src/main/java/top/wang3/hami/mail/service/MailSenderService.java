package top.wang3.hami.mail.service;

import lombok.extern.slf4j.Slf4j;
import top.wang3.hami.mail.config.CustomMailProperties;
import top.wang3.hami.mail.model.MailSendResult;
import top.wang3.hami.mail.model.PrepareMimeMessageHelper;
import top.wang3.hami.mail.supplier.MailSenderManager;

@Slf4j
public class MailSenderService {

    private final MailSenderManager supplier;

    CustomMailProperties config;

    public MailSenderService(MailSenderManager supplier, CustomMailProperties config) {
        this.config = config;
        this.supplier = supplier;
    }

    /**
     * 根据strategy获取MailService
     * @return MessageWrapper
     */
    public PrepareMimeMessageHelper.MessageWrapper of() {
        PrepareMimeMessageHelper helper = new PrepareMimeMessageHelper(supplier);
        return helper.create();
    }

    /**
     * 指定某个MailService发送邮件
     * 不影响默认的调度方式
     * @param key MailService的key
     * @return MessageWrapper
     */
    public PrepareMimeMessageHelper.MessageWrapper of(String key) {
        var sender = supplier.getMailSender(key);
        PrepareMimeMessageHelper helper = new PrepareMimeMessageHelper(supplier, sender);
        return helper.create();
    }

    public MailSendResult sendText(String subject, String text, String ...to) {
       return new PrepareMimeMessageHelper(supplier, supplier.getMailSender())
                .create()
                .to(to)
                .subject(subject)
                .text(text)
                .send();
    }

    public MailSendResult sendHtml(String subject, String html, String ...to) {
        return new PrepareMimeMessageHelper(supplier, supplier.getMailSender())
                .create()
                .to(to)
                .subject(subject)
                .html(html)
                .send();
    }

}
