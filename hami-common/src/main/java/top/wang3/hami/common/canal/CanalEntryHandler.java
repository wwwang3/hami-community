package top.wang3.hami.common.canal;

/**
 *
 * Canal.Entry处理器 将RowData.columnList ===> T
 */
public interface CanalEntryHandler<T> {

    void processInsert(T entity);

    void processUpdate(T before, T after);

    void processDelete(T deletedEntity);
}
