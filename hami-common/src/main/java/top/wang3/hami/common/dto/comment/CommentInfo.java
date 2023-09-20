package top.wang3.hami.common.dto.comment;


import lombok.Data;
import lombok.EqualsAndHashCode;
import top.wang3.hami.common.model.Comment;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommentInfo extends Comment {

    private String parentContent;
}
