package top.wang3.hami.common.dto.comment;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 发表评论参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentParam {

    /**
     * 文章ID
     */
    @NotNull
    private Integer articleId;

    /**
     * 根评论ID
     */
    @NotNull(groups = Reply.class)
    @Min(value = 1, groups = Reply.class)
    private Integer rootId;

    /**
     * 父评论ID
     */
    @NotNull(groups = Reply.class)
    @Min(value = 1, groups = Reply.class)
    private Integer parentId;

    @NotNull
    private String content;

    private List<String> pictures;

    /**
     * 发表评论时, 指定的groups
     */
    public interface Reply {}
}
