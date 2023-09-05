package top.wang3.hami.common.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class ArticlePageParam extends PageParam {

    private Integer cateId;
    private Integer tagId;

}
