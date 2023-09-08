package top.wang3.hami.message.canal;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.wang3.hami.message.annotation.CanalListener;

import java.util.HashMap;
import java.util.Map;

@Component
@SuppressWarnings(value = {"unchecked", "rawtypes"})
public class SimpleCanalHandlerFactory implements CanalEntryHandlerFactory, ApplicationContextAware {

    private Map<String, CanalEntryHandler<?>> handlerMap;

    @Override
    public <T> CanalEntryHandler<T> getHandler(String tableName) {
        return handlerMap == null ? null : (CanalEntryHandler<T>) handlerMap.get(tableName);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, CanalEntryHandler> handlers = applicationContext.getBeansOfType(CanalEntryHandler.class);
        if (handlers.isEmpty()) return;
        handlerMap = new HashMap<>();
        handlers.forEach((k, v) -> {
            CanalListener annotation = v.getClass().getAnnotation(CanalListener.class);
            if (annotation != null && StringUtils.hasText(annotation.value())) {
                handlerMap.putIfAbsent(annotation.value(), v);
            }
        });
    }
}
