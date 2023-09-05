package top.wang3.hami.message.handler;

public interface CanalEntryHandlerFactory {


    default <T> CanalEntryHandler<T> getHandler(String tableName) {
        return null;
    }


}
