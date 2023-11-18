package top.wang3.hami.core.service.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import top.wang3.hami.core.service.mail.template.MailTemplate;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
@SuppressWarnings(value = "rawtypes")
@Slf4j
public class MailTemplateFactory implements ApplicationContextAware {

    private final Map<Class<?>, MailTemplate<?>> templateMap = new ConcurrentHashMap<>(16);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, MailTemplate> templates = applicationContext.getBeansOfType(MailTemplate.class);
        templates.values()
                .forEach(template -> {
                    Type[] types = template.getClass().getGenericInterfaces();
                    for (Type t : types) {
                        Class c = (Class) ((ParameterizedType) t).getRawType();
                        if (c.equals(MailTemplate.class)) {
                            Class<?> actualTypeArgument = (Class<?>) ((ParameterizedType) t).getActualTypeArguments()[0];
                            MailTemplate<?> old = templateMap.putIfAbsent(actualTypeArgument, template);
                            if (old != null && old != template) {
                                throw new IllegalArgumentException("multi template found for type: " + actualTypeArgument
                                        + "[" +  old.getClass() + "," + template.getClass() + "]");
                            }
                        }
                    }
                });
        log.info("found {} mail template", templateMap.size());
    }


    public MailTemplate<?> getTemplate(Class<?> clazz) {
        MailTemplate<?> template = templateMap.get(clazz);
        if (template == null) {
            Class<?> superclass = clazz.getSuperclass();
            return templateMap.get(superclass);
        }
        return template;
    }
}
