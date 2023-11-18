package top.wang3.hami.core.service.mail.template;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.message.email.AlarmEmailMessage;
import top.wang3.hami.core.HamiProperties;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AlarmEmailTemplate implements MailTemplate<AlarmEmailMessage> {

    private final HamiProperties properties;

    @Override
    public List<String> getReceivers(AlarmEmailMessage item) {
        return List.of(properties.getEmail());
    }

    @Override
    public String getSubject(AlarmEmailMessage item) {
        return item.getSubject();
    }

    @Override
    public String render(AlarmEmailMessage item) {
        return item.getMsg();
    }

    @Override
    public boolean html() {
        return false;
    }
}
