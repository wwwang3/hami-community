package top.wang3.hami.canal.config;


import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import top.wang3.hami.canal.SimpleCanalHandlerFactory;
import top.wang3.hami.canal.annotation.CanalRabbitHandlerAnnotationBeanPostProcessor;


/**
 * Factory和Handler配置
 */
@SuppressWarnings("all")
public class CanalRegistrar implements ImportBeanDefinitionRegistrar {

    public static final String CANAL_RABBIT_HANDLER_BEAN_POST_PROCESSOR =
            "top.wang3.hami.canal.annotation.CanalRabbitHandlerAnnotationBeanPostProcessor";
    public static final String CANAL_ENTRY_HANDLER_FACTORY = "top.wang3.hami.canal.SimpleCanalHandlerFactory";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        registry.registerBeanDefinition(CANAL_ENTRY_HANDLER_FACTORY,
                new RootBeanDefinition(SimpleCanalHandlerFactory.class));
        registry.registerBeanDefinition(CANAL_RABBIT_HANDLER_BEAN_POST_PROCESSOR,
                new RootBeanDefinition(CanalRabbitHandlerAnnotationBeanPostProcessor.class));
    }
}
