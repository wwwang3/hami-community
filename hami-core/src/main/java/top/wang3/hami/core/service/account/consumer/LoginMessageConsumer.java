package top.wang3.hami.core.service.account.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.message.user.LoginRabbitMessage;
import top.wang3.hami.common.model.LoginRecord;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.service.account.repository.LoginRecordRepository;

import java.util.List;

@RabbitListener(
        id = "AccountMsgContainer-2",
        bindings = {
                @QueueBinding(
                        value = @Queue("hami-account-queue-2"),
                        exchange = @Exchange(value = RabbitConstants.HAMI_ACCOUNT_EXCHANGE, type = "topic"),
                        key = {"login.success"}
                ),
        },
        containerFactory = RabbitConstants.BATCH_LISTENER_FACTORY
)
@RequiredArgsConstructor
@Component
@Slf4j
public class LoginMessageConsumer {

    private final LoginRecordRepository loginRecordRepository;

    @RabbitHandler
    public void handleLoginEvent(List<LoginRabbitMessage> messages) {
        try {
            List<LoginRecord> records = ListMapperHandler.listTo(messages, message -> {
                LoginRecord record = new LoginRecord();
                record.setUserId(message.getId());
                record.setLoginTime(message.getLoginTime());
                record.setIpInfo(message.getIpInfo());
                return record;
            });
            long rows = loginRecordRepository.batchInsertRecords(records);
            if (log.isDebugEnabled()) {
                log.debug("insert {} login-record", rows);
            }
        } catch (Exception e) {
            log.warn("batch insert login-record failed, error_class: {}, error_msg: {}", e.getClass(), e.getMessage());
        }
    }

}
