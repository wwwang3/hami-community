package top.wang3.hami.web;

import cn.xuyanwu.spring.file.storage.spring.EnableFileStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.article.ArticleStatDTO;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.HamiCoreConfig;
import top.wang3.hami.security.EnableSecurity;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@Import(value = {HamiCoreConfig.class})
@EnableSecurity
@EnableTransactionManagement
@EnableFileStorage
@EnableCaching
@EnableAspectJAutoProxy
@Slf4j
public class HamiCommunityApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(HamiCommunityApplication.class, args);
//        test2();
        RedisClient.setCacheObject("test", "114514");
    }

    public static void test1() {
        long start = System.currentTimeMillis();

        ArrayList<String> list = new ArrayList<>(20);
        for (int i = 0; i < 20; i++) {
            list.add(Constants.COUNT_TYPE_ARTICLE + (65980 + i));
        }
        List<ArticleStatDTO> object = RedisClient.getMultiCacheObject(list);
        long end = System.currentTimeMillis();
        log.info("test multi get cost: {}", end - start);
    }

    public static void test2() {
        long start = System.currentTimeMillis();

        ArrayList<ArticleStatDTO> list = new ArrayList<>(20);
        for (int i = 0; i < 20; i++) {
            String key = Constants.COUNT_TYPE_ARTICLE + (65980 + i);
            ArticleStatDTO dto = RedisClient.getCacheObject(key);
            list.add(dto);
        }
        long end = System.currentTimeMillis();
        log.info("test multi get cost: {}", end - start);
    }


}