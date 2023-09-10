package top.wang3.hami.common.canal;

import java.util.List;

public interface CanalEntryHandlerFactory {


    default List<CanalEntryHandler<?>> getHandler(String tableName) {
        return null;
    }


}
