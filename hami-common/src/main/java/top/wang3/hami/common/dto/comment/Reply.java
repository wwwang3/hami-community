package top.wang3.hami.common.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.model.Comment;

import java.util.List;

/**
 * 回复列表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Reply {

    /**
     * 子评论总数
     */
    private long total;

    /**
     * 子评论总数
     */
    private List<Comment> comments;
}
