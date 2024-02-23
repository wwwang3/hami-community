package top.wang3.hami.mail.service;

import lombok.extern.slf4j.Slf4j;
import top.wang3.hami.mail.config.CustomMailProperties;
import top.wang3.hami.mail.manager.MailSenderManager;
import top.wang3.hami.mail.model.MailSendResult;
import top.wang3.hami.mail.model.PrepareMimeMessageHelper;

import java.util.List;

@Slf4j
@SuppressWarnings("unused")
public class MailSenderService {

    private final MailSenderManager manager;

    CustomMailProperties config;

    public MailSenderService(MailSenderManager manager, CustomMailProperties config) {
        this.config = config;
        this.manager = manager;
    }

    /**
     * 根据strategy获取MailService
     * @return MessageWrapper
     */
    public PrepareMimeMessageHelper.MessageWrapper of() {
        PrepareMimeMessageHelper helper = new PrepareMimeMessageHelper(manager);
        return helper.create();
    }

    /**
     * 指定某个MailService发送邮件
     * 不影响默认的调度方式
     * @param key MailService的key
     * @return MessageWrapper
     */
    public PrepareMimeMessageHelper.MessageWrapper of(String key) {
        var sender = manager.getMailSender(key);
        PrepareMimeMessageHelper helper = new PrepareMimeMessageHelper(manager, sender);
        return helper.create();
    }

    public MailSendResult sendText(String subject, String text, String ...receivers) {
       return new PrepareMimeMessageHelper(manager, manager.getMailSender())
                .create()
                .to(receivers)
                .subject(subject)
                .text(text)
                .send();
    }

    public MailSendResult sendHtml(String subject, String html, String ...receivers) {
        return new PrepareMimeMessageHelper(manager, manager.getMailSender())
                .create()
                .to(receivers)
                .subject(subject)
                .html(html)
                .send();
    }

    public MailSendResult sendText(String subject, String text, List<String> receivers) {
        return new PrepareMimeMessageHelper(manager, manager.getMailSender())
                .create()
                .to(receivers)
                .subject(subject)
                .text(text)
                .send();
    }

    public MailSendResult sendHtml(String subject, String text, List<String> receivers) {
        return new PrepareMimeMessageHelper(manager, manager.getMailSender())
                .create()
                .to(receivers)
                .subject(subject)
                .text(text)
                .send();
    }

}
