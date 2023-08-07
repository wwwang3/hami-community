package top.wang3.hami.common.component;

public interface Ip2RegionSearcher {

    String search(String ip);

    void destroy() throws Exception;
}
