package top.wang3.hami.canal.listener;

import lombok.Setter;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.listener.AbstractRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.lang.NonNull;
import top.wang3.hami.canal.CanalEntryHandlerFactory;
import top.wang3.hami.canal.converter.CanalMessageConverter;

@Setter
public class CanalListenerEndpoint extends AbstractRabbitListenerEndpoint {

    private CanalEntryHandlerFactory canalEntryHandlerFactory;

    private CanalMessageConverter canalMessageConverter;

    @Override
    @NonNull
    protected MessageListener createMessageListener(@NonNull MessageListenerContainer container) {
        return new CanalMessageListener(getId(), canalEntryHandlerFactory, canalMessageConverter);
    }
}
