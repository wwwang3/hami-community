package top.wang3.hami.security.annotation.provider;

import top.wang3.hami.security.annotation.ApiInfo;

import java.util.List;

public interface ApiInfoProvider {

    List<ApiInfo> getApis();
}
