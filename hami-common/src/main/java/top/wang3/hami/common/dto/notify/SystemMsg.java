package top.wang3.hami.common.dto.notify;


import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 系统消息
 */
@Data
@AllArgsConstructor
public class SystemMsg {

    private int userId;
    private String content;
}
