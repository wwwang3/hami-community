package top.wang3.hami.common.dto.request;


import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserArticleParam extends PageParam {

    @Min(value = 1)
    private int userId;
}
