package top.wang3.hami.common.message.email;


import lombok.Data;

@Data
public class AlarmEmailMessage implements EmailRabbitMessage {

    private String subject;

    private String msg;

    public AlarmEmailMessage(String subject, String msg) {
        this.subject = subject;
        this.msg = msg;
    }

    public AlarmEmailMessage() {
    }
}
