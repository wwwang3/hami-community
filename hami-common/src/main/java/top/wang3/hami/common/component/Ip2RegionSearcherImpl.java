package top.wang3.hami.common.component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import top.wang3.hami.common.util.IpUtils;

import java.io.IOException;
import java.io.InputStream;

@Component
@ConditionalOnWebApplication
@Slf4j
public class Ip2RegionSearcherImpl implements Ip2RegionSearcher {

    public static final String XDB_FILE = "classpath:ip2region/ip2region.xdb";
    private final ResourceLoader resourceLoader;

    private Searcher searcher;

    public Ip2RegionSearcherImpl(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }


    @PostConstruct
    public void init() {
        log.debug("start to load ip2region.xdb");
        Resource resource = resourceLoader.getResource(XDB_FILE);
        try (InputStream stream = resource.getInputStream()) {
            this.searcher = Searcher.newWithBuffer(StreamUtils.copyToByteArray(stream));
            IpUtils.register(this);
            log.debug("success load ip2region.xdb");
        } catch (IOException e) {
            log.warn("load ip2region.xdb failed: {}", e.getMessage());
        }
    }

    @PreDestroy
    @Override
    public void destroy() throws Exception {
        if (this.searcher != null) {
            this.searcher.close();
            log.debug("destroy ip-searcher success");
        }
    }

    @SneakyThrows(Exception.class)
    public String search(String ip) {
        return searcher.search(ip);
    }

}
