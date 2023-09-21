package top.wang3.hami.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IpInfo {

    public static final String UNKNOWN_IP = "unknown";

    /**
     * IPv4地址
     */
    private String ip;
    /**
     * 国家
     */
    private String country;
    /**
     * 省
     */
    private String province;
    /**
     * 城市
     */
    private String city;
    /**
     * 区域
     */
    private String area;
    /**
     * 运营商
     */
    private String isp;

    public IpInfo(String ip) {
        this.ip = ip;
    }
}
