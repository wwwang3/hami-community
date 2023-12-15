package top.wang3.hami.canal.converter;


import top.wang3.hami.canal.annotation.CanalEntity;

import java.util.List;
import java.util.Map;

@SuppressWarnings(value = {"unchecked", "rawtypes"})
public interface CanalMessageConverter {

    /**
     * 每个table一个List
     *
     * @param bytes 数据
     * @return Map<String, List <CanalEntity <T>>>
     */
    <T> Map<String, List<CanalEntity<T>>> convertToEntity(byte[] bytes);

}
