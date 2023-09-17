package top.wang3.hami.common.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentParam {

    @NotNull
    private Integer articleId;

    @NotNull(groups = Reply.class)
    @Min(value = 1, groups = Reply.class)
    private Integer rootId;

    @NotNull(groups = Reply.class)
    @Min(value = 1, groups = Reply.class)
    private Integer parentId;

    @NotNull
    private String content;
    private String contentImg;

    public interface Reply {}
}
