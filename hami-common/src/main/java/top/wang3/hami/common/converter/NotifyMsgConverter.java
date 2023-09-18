package top.wang3.hami.common.converter;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import top.wang3.hami.common.dto.notify.ArticleMsgDTO;
import top.wang3.hami.common.dto.notify.CommentMsgDTO;
import top.wang3.hami.common.dto.notify.DiggMsgDTO;
import top.wang3.hami.common.dto.notify.FollowMsgDTO;
import top.wang3.hami.common.model.NotifyMsg;

import java.util.List;

@Mapper
public interface NotifyMsgConverter {

    NotifyMsgConverter INSTANCE = Mappers.getMapper(NotifyMsgConverter.class);


    @Mapping(target = "userId", source = "sender")
    @Mapping(target = "user", ignore = true)
    FollowMsgDTO toFollowMsg(NotifyMsg msg);

    List<FollowMsgDTO> toFollowMsgs(List<NotifyMsg> notifyMsgs);

    @Mapping(target = "userId", source = "sender")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "content", ignore = true)
    @Mapping(target = "commentId", ignore = true)
    @Mapping(target = "articleId", source = "itemId")
    DiggMsgDTO toDiggMsg(NotifyMsg notifyMsg);

    List<DiggMsgDTO> toDiggMsgs(List<NotifyMsg> msgs);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "userId", source = "sender")
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "content", source = "detail")
    @Mapping(target = "commentId", source = "relatedId")
    @Mapping(target = "articleId", source = "itemId")
    CommentMsgDTO toCommentMsg(NotifyMsg msg);

    List<CommentMsgDTO> toCommentMsgs(List<NotifyMsg> msgs);

    @Mapping(target = "userId", source = "sender")
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "articleId", source = "itemId")
    ArticleMsgDTO toArticleMsgDTO(NotifyMsg notifyMsg);

    List<ArticleMsgDTO> toArticleMsgDTOs(List<NotifyMsg> msgs);
}
