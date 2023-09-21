package top.wang3.hami.security.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import top.wang3.hami.common.dto.IpInfo;

import java.util.Optional;

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

    public static Optional<String> getOptIp() {
        IpInfo ipInfo = CONTEXT.get();
        if (ipInfo == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(ipInfo.getIp());
    }

    public static String getIp() {
        IpInfo ipInfo = CONTEXT.get();
        if (ipInfo != null) return ipInfo.getIp();
        return null;
    }

    public static String getIpDefaultUnknown() {
        return getOptIp()
                .orElse(IpInfo.UNKNOWN_IP);
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
