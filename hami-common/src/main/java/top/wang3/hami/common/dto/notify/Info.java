package top.wang3.hami.common.dto.notify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * info
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Info {

    /**
     * 关联元素的ID
     */
    private Integer id;

    /**
     * 名称 标题, 用户名等
     */
    private String name;

    /**
     * 图像
     */
    private String image;

    /**
     * 内容
     */
    private String detail;

    /**
     * 是否关注 只对用户信息有用
     */
    private boolean followed;
}