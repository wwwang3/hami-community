package top.wang3.hami.canal.annotation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import top.wang3.hami.canal.CanalEntryHandler;
import top.wang3.hami.canal.CanalEntryHandlerFactory;
import top.wang3.hami.common.util.AopTargetUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Slf4j
@SuppressWarnings(value = {"unchecked", "rawtypes"})
public class CanalRabbitHandlerAnnotationBeanPostProcessor
        implements BeanPostProcessor {


    private final CanalEntryHandlerFactory canalEntryHandlerFactory;

    @Lazy
    public CanalRabbitHandlerAnnotationBeanPostProcessor(CanalEntryHandlerFactory canalEntryHandlerFactory) {
        this.canalEntryHandlerFactory = canalEntryHandlerFactory;
    }

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
            throw new IllegalArgumentException("failed to process canal listener.", e);
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


    /**
     * 获取CanalEntryHandler泛型接口的参数, 当直接实现时, 可以获取准确,
     * 当继承抽象类, 抽象类直接实现接口时, 会获取超类的第一个泛型参数的类型, 可能后续实体转化时会失败
     * @param handler CanalEntryHandler
     * @return Class<T> 实体类型
     * @param <T> 实体泛型
     */
    private <T> Class<T> resolveTableClass(CanalEntryHandler<T> handler) {
        Class<? extends CanalEntryHandler> handlerClass = handler.getClass();
        // 获取handler实现的接口类型
        Type[] interfacesTypes = handlerClass.getGenericInterfaces();
        if (interfacesTypes.length == 0) {
            // 为空获取其直接超类的泛型参数, 返回第一个
            ParameterizedType type = (ParameterizedType) handlerClass.getGenericSuperclass();
            return (Class<T>) type.getActualTypeArguments()[0];
        }
        for (Type t : interfacesTypes) {
            Class clazz = (Class) ((ParameterizedType) t).getRawType();
            if (clazz.equals(CanalEntryHandler.class)) {
                return (Class<T>) ((ParameterizedType) t).getActualTypeArguments()[0];
            }
        }
        return null;
    }
}
