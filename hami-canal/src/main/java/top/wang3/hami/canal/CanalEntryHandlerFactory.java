package top.wang3.hami.canal;

import org.springframework.lang.NonNull;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"all"})
public interface CanalEntryHandlerFactory {


    /**
     * 根据table名称获取table对应的实体类型
     * @param tableName 表名
     * @return table对应实体的类型
     */
    <T> Class<T> getTableClass(String tableName);

    Class<?> getHandlerEntityCalss(CanalEntryHandler<?> handler);

    /**
     * 获取容器专属的CanalEntryHandler
     * @param containerId 容器ID
     * @return List<CanalEntryHandler<?>>
     */
    List<CanalEntryHandler<?>> getContainerHandlers(String containerId, String tableName);

    /**
     * 根据表名获取未指定Container-id的CanalEntryHandler
     * @param tableName 表名
     * @return List<CanalEntryHandler<?>>
     */
    List<CanalEntryHandler<?>> getHandler(String tableName);

    Map<String, Field> getTableField(String tableName);

    boolean addTableClass(@NonNull String tableName, Class<?> tableClass);

    boolean addHandlerEntityClass(Class<? extends CanalEntryHandler> handlerClass, Class<?> tableClass);

    boolean addCanalEntryHandler(@NonNull String tableName, String containerId, CanalEntryHandler<?> handler);

}
