package top.wang3.hami.canal.annotation;


import com.alibaba.otter.canal.protocol.CanalEntry;
import lombok.Data;

@Data
public class CanalEntity<T> {

    private String tableName;
    private Class<T> tableClass;
    private CanalEntry.EventType type;

    private T before;
    private T after;
}
