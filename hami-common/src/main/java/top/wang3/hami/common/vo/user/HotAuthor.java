package top.wang3.hami.common.vo.user;


import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("user_id")
    private Integer userId;

    /**
     * 热度值
     */
    @JsonProperty("hot_index")
    private Double hotIndex;

    /**
     * 用户信息
     */
    private UserVo user;
}
