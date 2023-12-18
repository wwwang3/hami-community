package top.wang3.hami.canal.annotation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import top.wang3.hami.canal.CanalEntryHandler;
import top.wang3.hami.canal.CanalEntryHandlerFactory;
import top.wang3.hami.common.util.AopTargetUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings(value = {"unchecked", "rawtypes"})
public class CanalRabbitHandlerAnnotationBeanPostProcessor
        implements BeanPostProcessor {


    private final CanalEntryHandlerFactory canalEntryHandlerFactory;


    @Override
    public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        if (!CanalEntryHandler.class.isAssignableFrom(targetClass)) {
            return bean;
        }
        try {
            CanalEntryHandler<?> handler = (CanalEntryHandler<?>) AopTargetUtils.getTarget(bean);
            CanalRabbitHandler canalRabbitHandler = targetClass.getAnnotation(CanalRabbitHandler.class);
            processCanalRabbitHandler(canalRabbitHandler, handler);
        } catch (Exception e) {
            throw new IllegalArgumentException("can not target object", e);
        }
        return bean;
    }

    private void processCanalRabbitHandler(CanalRabbitHandler canalRabbitHandler, CanalEntryHandler<?> handler) {
        Class<?> tableClass = resolveTableClass(handler);
        Class<? extends CanalEntryHandler> handlerClass = handler.getClass();
        if (tableClass == null) {
            log.warn("handler: {}, could not find handler`s tableClass,  ignore it", handlerClass);
            return;
        }
        String containerId = canalRabbitHandler.container();
        String tableName = canalRabbitHandler.value();
        if (StringUtils.hasText(tableName)) {
            canalEntryHandlerFactory.addTableClass(tableName, tableClass);
            canalEntryHandlerFactory.addHandlerEntityClass(handlerClass, tableClass);
            canalEntryHandlerFactory.addCanalEntryHandler(tableName, containerId, handler);
        }
    }

    private <T> Class<T> resolveTableClass(CanalEntryHandler<T> handler) {
        Class<? extends CanalEntryHandler> handlerClass = handler.getClass();
        Type[] interfacesTypes = handlerClass.getGenericInterfaces();
        for (Type t : interfacesTypes) {
            Class clazz = (Class) ((ParameterizedType) t).getRawType();
            if (clazz.equals(CanalEntryHandler.class)) {
                return (Class<T>) ((ParameterizedType) t).getActualTypeArguments()[0];
            }
        }
        return null;
    }
}
