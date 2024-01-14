package top.wang3.hami.common.vo.comment;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReplyVo {

    private long total;
    private List<CommentVo> list;
}
