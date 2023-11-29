package top.wang3.hami.common.canal;

import top.wang3.hami.common.constant.Constants;

/**
 *
 * Canal.Entry处理器 将RowData.columnList ===> T
 */
public interface CanalEntryHandler<T> {

    void processInsert(T entity);

    void processUpdate(T before, T after);

    void processDelete(T deletedEntity);

    static boolean isLogicDelete(Byte before, Byte after) {
        return Constants.NOT_DELETED.equals(before) && Constants.DELETED.equals(after);
    }
}
