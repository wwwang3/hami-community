package top.wang3.hami.common.converter;


import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import top.wang3.hami.common.dto.CommentDTO;
import top.wang3.hami.common.dto.notify.CommentMsg;
import top.wang3.hami.common.dto.notify.ReplyMsg;
import top.wang3.hami.common.dto.request.CommentParam;
import top.wang3.hami.common.model.Comment;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface CommentConverter {

    CommentConverter INSTANCE = Mappers.getMapper(CommentConverter.class);

    default CommentDTO toCommentDTO(Comment comment) {
        if (comment == null) return null;
        CommentDTO dto = new CommentDTO();
        dto.setArticleId(comment.getArticleId());
        dto.setId(comment.getId());
        dto.setUserId(comment.getUserId());
        dto.setComment(comment);
        return dto;
    }

    default List<CommentDTO> toCommentDTO(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) return new ArrayList<>();
        ArrayList<CommentDTO> dtos = new ArrayList<>(comments.size());
        for (Comment comment : comments) {
            CommentDTO dto = toCommentDTO(comment);
            dtos.add(dto);
        }
        return dtos;
    }

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

      default ReplyMsg toReplyMsg(Comment comment) {
          return ReplyMsg.builder()
                  .userId(comment.getUserId())
                  .replyTo(comment.getReplyTo())
                  .content(comment.getContent())
                  .articleId(comment.getArticleId())
                  .replyId(comment.getId())
                  .build();
      }

    default CommentMsg toCommentMsg(Comment comment) {
        return CommentMsg.builder()
                .userId(comment.getUserId())
                .commentId(comment.getId())
                .articleId(comment.getArticleId())
                .commentTo(comment.getReplyTo())
                .content(comment.getContent())
                .build();
    }
}
