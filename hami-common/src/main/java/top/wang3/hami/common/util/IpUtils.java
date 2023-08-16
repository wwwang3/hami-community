package top.wang3.hami.common.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import top.wang3.hami.common.component.Ip2RegionSearcher;
import top.wang3.hami.common.dto.IpInfo;

import java.util.Arrays;
import java.util.regex.Pattern;

@Slf4j
public final class IpUtils {

    /**
     * 缓存正则表达式，提升编译速度
     */
    private static final Pattern SPLIT_PATTERN = Pattern.compile("\\|");

    /**
     * ip2Region 采用 0 填充的没有数据的字段
     */
    private static final String ZERO = "0";

    private static final String[] IP_HEADER_FIELD = {"x-forwarded-for", "Proxy-Client-IP", "X-Forwarded-For",
            "WL-Proxy-Client-IP", "X-Real-IP"};

    private static Ip2RegionSearcher searcher;

    private IpUtils() {
    }

    public static void register(Ip2RegionSearcher h) {
        searcher = h;
    }

    public static IpInfo getIpInfo(HttpServletRequest request) {
        String ip = getIp(request);
        return getIpInfo(ip);
    }

    public static IpInfo getIpInfo(String ip) {
        if (ip == null) return new IpInfo("unknown");
        String dataBlock = searcher.search(ip);
        return parseIp(ip, dataBlock);
    }

    private static IpInfo parseIp(String ip, String dataBlock) {
        IpInfo info = new IpInfo(ip);
        if (dataBlock == null) {
            return info;
        }
        String[] blocks = SPLIT_PATTERN.split(dataBlock);
        //补齐
        if (blocks.length < 5) {
            blocks = Arrays.copyOf(blocks, 5);
        }
        info.setCountry(filterZero(blocks[0]));
        info.setArea(filterZero(blocks[1]));
        info.setProvince(filterZero(blocks[2]));
        info.setCity(filterZero(blocks[3]));
        info.setIsp(filterZero(blocks[4]));
        return info;
    }

    private static String filterZero(String block) {
        // null 或 0 返回 null
        if (block == null || ZERO.equals(block)) {
            return null;
        }
        return block;
    }

    /**
     * 获取IPV4地址
     * @param request HttpServletRequest
     * @return ip
     */
    public static String getIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String ip = null;
        for (String header: IP_HEADER_FIELD) {
            ip = request.getHeader(header);
            if (!isUnknown(ip)) {
                break;
            }
        }
        //still unknown
        if (isUnknown(ip)) {
            ip = request.getRemoteAddr();
        }

        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : getMultistageReverseProxyIp(ip);
    }

    /**
     * 从多级反向代理中获得第一个非unknown IP地址
     * @param ip 获得的IP地址
     * @return 第一个非unknown IP地址
     */
    private static String getMultistageReverseProxyIp(String ip) {
        // 多级反向代理检测
        if (ip != null && ip.indexOf(",") > 0) {
            final String[] ips = ip.trim().split(",");
            for (String subIp : ips) {
                if (!isUnknown(subIp)) {
                    ip = subIp;
                    break;
                }
            }
        }
        return ip;
    }

    /**
     * 检测给定字符串是否为未知，多用于检测HTTP请求相关
     * @param checkString 被检测的字符串
     * @return true -ip为空或者unknown
     */
    private static boolean isUnknown(String checkString) {
        return !StringUtils.hasText(checkString) || "unknown".equalsIgnoreCase(checkString);
    }


}
