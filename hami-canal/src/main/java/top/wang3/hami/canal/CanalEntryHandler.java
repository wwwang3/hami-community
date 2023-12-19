package top.wang3.hami.canal;

import top.wang3.hami.common.constant.Constants;

/**
 *
 * Canal.Entry处理器 将RowData.columnList ===> T or FlatMessage ===> T
 */
public interface CanalEntryHandler<T> {

    void processInsert(T entity);

    void processUpdate(T before, T after);

    void processDelete(T deletedEntity);

    default boolean isLogicDelete(Byte old, Byte now) {
        return Constants.ZERO.equals(old) && Constants.ONE.equals(now);
    }

}
