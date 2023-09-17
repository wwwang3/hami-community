package top.wang3.hami.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.model.Comment;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Reply {

    private long total;
    private List<Comment> comments;
}
