package top.wang3.hami.common.dto.comment;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.wang3.hami.common.dto.PageParam;

/**
 * 评论分页请求参数
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CommentPageParam extends PageParam {

    /**
     * 文章ID
     */
    @NotNull
    @Min(value = 1)
    @JsonProperty("article_id")
    private Integer articleId;

    /**
     * 根评论Id
     */
    @NotNull(groups = Reply.class)
    @JsonProperty("root_Id")
    private Integer rootId;

    /**
     * 排序字段 0-时间 1-点赞数
     */
    @Min(value = 0)
    @Max(value = 1)
    private Integer sort;

    /**
     * 请求回复时, 指定的groups
     */
    public interface Reply {

    }
}
