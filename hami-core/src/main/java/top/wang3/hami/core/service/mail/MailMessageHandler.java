package top.wang3.hami.core.service.mail;


import top.wang3.hami.mail.model.MailSendResult;


public interface MailMessageHandler {

    /**
     * 处理邮件消息, 根据message类型选择不同模板
     * @param message 待发送的邮件消息
     * @return MailSendResult
     */
    MailSendResult handle(Object message);

}
