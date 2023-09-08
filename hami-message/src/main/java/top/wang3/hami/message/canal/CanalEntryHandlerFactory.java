package top.wang3.hami.message.canal;

public interface CanalEntryHandlerFactory {


    default <T> CanalEntryHandler<T> getHandler(String tableName) {
        return null;
    }


}
