package top.wang3.hami.common.vo.user;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 热门作者
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotAuthor {

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 热度值
     */
    private Double hotIndex;

    /**
     * 用户信息
     */
    private UserVo user;
}
