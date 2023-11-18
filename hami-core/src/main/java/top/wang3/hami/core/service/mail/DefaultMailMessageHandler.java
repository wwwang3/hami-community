package top.wang3.hami.core.service.mail;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wang3.hami.core.service.mail.template.MailTemplate;
import top.wang3.hami.mail.model.MailSendResult;
import top.wang3.hami.mail.service.MailSenderService;

import java.util.List;

@Component
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class DefaultMailMessageHandler implements MailMessageHandler {


    private final MailSenderService mailSenderService;
    private final MailTemplateFactory mailTemplateFactory;

    @Override
    public MailSendResult handle(Object message) {
        if (message == null) {
            return new MailSendResult("", "message cannot empty", null);
        }
        MailTemplate<Object> template = (MailTemplate<Object>) mailTemplateFactory.getTemplate(message.getClass());
        if (template == null) {
            return new MailSendResult(null, "no template found for type:" + message.getClass(), null);
        }
        String subject = template.getSubject(message);
        String content = template.render(message);
        List<String> receivers = template.getReceivers(message);
        if (template.html()) {
            return mailSenderService.sendHtml(subject, content, receivers);
        } else {
            return mailSenderService.sendText(subject, content, receivers);
        }
    }
}
