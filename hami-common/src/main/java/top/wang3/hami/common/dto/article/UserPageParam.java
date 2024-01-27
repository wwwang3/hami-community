package top.wang3.hami.common.dto.article;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.dto.PageParam;

/**
 * 用户分页请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserPageParam extends PageParam {

    /**
     * 用户ID
     */
    @Min(value = 1)
    @NotNull
    @JsonProperty("user_id")
    private int userId;
}
