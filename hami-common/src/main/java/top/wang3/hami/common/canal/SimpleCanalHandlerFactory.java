package top.wang3.hami.common.canal;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.OrderComparator;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.annotation.CanalListener;

import java.util.*;

@Component
@SuppressWarnings(value = {"rawtypes"})
public class SimpleCanalHandlerFactory implements CanalEntryHandlerFactory, ApplicationContextAware {

    private Map<String, List<CanalEntryHandler<?>>> handlerMap;

    @Override
    public List<CanalEntryHandler<?>> getHandler(String tableName) {
        return handlerMap == null ? Collections.emptyList() : handlerMap.get(tableName);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, CanalEntryHandler> handlers = applicationContext.getBeansOfType(CanalEntryHandler.class);
        if (handlers.isEmpty()) return;
        handlerMap = new HashMap<>();

        handlers.forEach((k, v) -> {
            CanalListener annotation = v.getClass().getAnnotation(CanalListener.class);
            List<CanalEntryHandler<?>> handler = handlerMap.computeIfAbsent(annotation.value(), (key) -> new ArrayList<>());
            handler.add(v);
        });
        //排序
        handlerMap.values().forEach(OrderComparator::sort);
    }
}
