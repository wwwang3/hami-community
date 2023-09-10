package top.wang3.hami.security.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import top.wang3.hami.common.dto.IpInfo;

/**
 * IP上下文
 */
public class IpContext {

    private static final ThreadLocal<IpInfo> CONTEXT = new TransmittableThreadLocal<>();

    public static void setIpInfo(IpInfo info) {
        CONTEXT.set(info);
    }

    public static IpInfo getIpInfo() {
        return CONTEXT.get();
    }

    public static String getIp() {
        IpInfo ipInfo = CONTEXT.get();
        if (ipInfo != null) return ipInfo.getIp();
        return null;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
