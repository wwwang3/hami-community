package top.wang3.hami.canal.config;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

@SuppressWarnings("all")
public class CanalRegistrarSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{HamiCanalRegistrar.class.getName()};
    }
}
