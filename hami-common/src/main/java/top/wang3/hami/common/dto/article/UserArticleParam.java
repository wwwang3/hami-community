package top.wang3.hami.common.dto.article;


import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.dto.PageParam;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserArticleParam extends PageParam {

    @Min(value = 1)
    private int userId;
}
