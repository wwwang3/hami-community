package top.wang3.hami.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import top.wang3.hami.core.HamiCoreConfig;

@SpringBootApplication
@Import(value = {HamiCoreConfig.class})
@Slf4j
public class HamiCommunityApplication {

    public static void main(String[] args) {
        SpringApplication.run(HamiCommunityApplication.class, args);
        long pid = ProcessHandle.current().pid();
        log.info("##Hami## PID: {}", pid);
    }

}