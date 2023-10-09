package top.wang3.hami.common.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class ArticlePageParam extends PageParam {

    @Min(value = 10000)
    @Max(value = 10007)
    private Integer cateId;
    private Integer tagId;

}
