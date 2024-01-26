package top.wang3.hami.common.dto.article;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.wang3.hami.common.dto.PageParam;

/**
 * 文章分页请求参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class ArticlePageParam extends PageParam {

    /**
     * 分类ID
     */
    @Min(value = 10000)
    @Max(value = 10007)
    @JsonProperty("cate_id")
    private Integer cateId;

    /**
     * 标签Id
     * @future 根据标签查询文章
     */
    @JsonProperty("tag_id")
    private Integer tagId;

}
