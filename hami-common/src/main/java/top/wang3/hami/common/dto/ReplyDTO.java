package top.wang3.hami.common.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReplyDTO {

    private long total;
    private List<CommentDTO> list;
}
