package top.wang3.hami.mail.supplier;

import top.wang3.hami.mail.sender.CustomMailSender;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮循策略
 */
public class RoundRobinMailSenderSupplier extends AbstractMailSenderSupplier {

    private final AtomicInteger current = new AtomicInteger(0);

    public RoundRobinMailSenderSupplier(List<CustomMailSender> senders) {
        super(senders);
    }

    @Override
    public CustomMailSender getMailSender() {
        int index = current.getAndUpdate(prev -> (prev + 1) % getSize());
        return getSenders().get(index);
    }
}
