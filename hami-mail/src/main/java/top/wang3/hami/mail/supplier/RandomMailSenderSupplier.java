package top.wang3.hami.mail.supplier;

import top.wang3.hami.mail.sender.CustomMailSender;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机策略
 */
public class RandomMailSenderSupplier extends AbstractMailSenderSupplier {

    public RandomMailSenderSupplier(List<CustomMailSender> senders) {
        super(senders);
    }

    @Override
    public CustomMailSender getMailSender() {
        int index = ThreadLocalRandom.current().nextInt(getSize());
        return getSenders().get(index);
    }

}
