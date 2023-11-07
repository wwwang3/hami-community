package top.wang3.hami.common.dto.comment;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.wang3.hami.common.dto.PageParam;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CommentPageParam extends PageParam {

    @NotNull
    @Min(value = 1)
    private Integer articleId;

    @NotNull(groups = Reply.class)
    private Integer rootId;

    @Min(value = 0)
    @Max(value = 1)
    private Integer sort;

    public interface Reply {

    }
}
