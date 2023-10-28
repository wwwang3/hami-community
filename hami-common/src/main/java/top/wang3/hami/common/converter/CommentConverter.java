package top.wang3.hami.common.converter;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import top.wang3.hami.common.dto.comment.CommentDTO;
import top.wang3.hami.common.dto.request.CommentParam;
import top.wang3.hami.common.model.Comment;

import java.util.List;

@Mapper
public interface CommentConverter {

    CommentConverter INSTANCE = Mappers.getMapper(CommentConverter.class);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "reply", ignore = true)
    @Mapping(target = "liked", ignore = true)
    @Mapping(target = "replyUser", ignore = true)
    CommentDTO toCommentDTO(Comment comment);

    List<CommentDTO> toCommentDTOList(List<Comment> comments);

    default Comment toComment(CommentParam param) {
        Comment comment = new Comment();
        comment.setContent(param.getContent());
        comment.setContentImg(param.getContentImg());
        comment.setArticleId(param.getArticleId());
        comment.setReplyTo(null);
        comment.setRootId(0);
        comment.setParentId(0); //默认为根评论(一级评论)
        return comment;
    }

}
