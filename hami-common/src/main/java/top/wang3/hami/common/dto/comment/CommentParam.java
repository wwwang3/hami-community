package top.wang3.hami.common.dto.comment;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @JsonProperty("article_id")
    private Integer articleId;

    /**
     * 根评论ID
     */
    @NotNull(groups = Reply.class)
    @Min(value = 1, groups = Reply.class)
    @JsonProperty("root_id")
    private Integer rootId;

    /**
     * 父评论ID
     */
    @NotNull(groups = Reply.class)
    @Min(value = 1, groups = Reply.class)
    @JsonProperty("parent_id")
    private Integer parentId;

    @NotNull
    private String content;

    @JsonProperty("content_img")
    private String contentImg;

    /**
     * 发表评论时, 指定的groups
     */
    public interface Reply {}
}
