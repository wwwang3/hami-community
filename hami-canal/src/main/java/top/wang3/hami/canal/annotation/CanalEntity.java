package top.wang3.hami.canal.annotation;


import com.alibaba.otter.canal.protocol.CanalEntry;
import lombok.Data;

@Data
public class CanalEntity<T> {

    private String tableName;
    private Class<T> tableClass;
    private CanalEntry.EventType type;

    /**
     * 插入时为空
     * 更新时为修改前的记录
     * 删除时为删除的记录
     */
    private T before;

    /**
     * 插入时为新增的记录
     * 更新时为修改后的记录
     * 删除时为空
     */
    private T after;
}
