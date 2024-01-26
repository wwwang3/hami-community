package top.wang3.hami.common.vo.comment;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 子评论
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReplyVo {

    /**
     * 子评论总数
     */
    private long total;

    /**
     * 子评论列表
     */
    private List<CommentVo> list;
}
