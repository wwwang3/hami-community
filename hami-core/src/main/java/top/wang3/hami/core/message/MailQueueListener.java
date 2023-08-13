package top.wang3.hami.core.message;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener
public class MailQueueListener {
}
