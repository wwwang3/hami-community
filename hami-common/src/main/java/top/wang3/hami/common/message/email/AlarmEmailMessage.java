package top.wang3.hami.common.message.email;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlarmEmailMessage implements EmailRabbitMessage {

    private String subject;

    private String msg;
}
