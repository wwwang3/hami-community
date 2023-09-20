package top.wang3.hami.core.service.common;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.converter.NotifyMsgConverter;
import top.wang3.hami.common.dto.ArticleInfo;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.UserDTO;
import top.wang3.hami.common.dto.builder.UserOptionsBuilder;
import top.wang3.hami.common.dto.notify.*;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.model.Comment;
import top.wang3.hami.common.model.NotifyMsg;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.mapper.NotifyMsgMapper;
import top.wang3.hami.core.repository.CommentRepository;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotifyMsgService extends ServiceImpl<NotifyMsgMapper, NotifyMsg> {

    public static final String[] FIELDS = {
            "id", "item_id", "item_name", "related_id",
            "sender", "receiver", "detail", "`type`", "`state`",
            "ctime", "mtime"
    };


    private final UserService userService;
    private final ArticleService articleService;
    private final CommentRepository commentRepository;

    @Transactional
    public boolean saveMsg(NotifyMsg msg) {
        return super.save(msg);
    }

    public boolean checkExist(Integer sender, Integer receiver, Integer type) {
        QueryWrapper<NotifyMsg> wrapper = Wrappers.query(new NotifyMsg())
                .eq("sender", sender)
                .eq("receiver", receiver)
                .eq("type", type);
        return getBaseMapper().exists(wrapper);
    }

    public PageData<NotifyMsg> listSystemMsg(PageParam param) {
        Page<NotifyMsg> page = param.toPage();
        int loginUserId = LoginUserContext.getLoginUserId();
        List<NotifyMsg> msgs = ChainWrappers.queryChain(getBaseMapper())
                .select(FIELDS)
                .eq("receiver", loginUserId)
                .eq("type", NotifyType.SYSTEM.getType())
                .orderByDesc("id")
                .list(page);
        page.setRecords(msgs);
        return PageData.build(page);
    }

    public PageData<CommentMsgDTO> listCommentMsg(PageParam param) {
        Page<NotifyMsg> page = param.toPage();
        int loginUserId = LoginUserContext.getLoginUserId();
        List<NotifyMsg> msgs = ChainWrappers.queryChain(getBaseMapper())
                .eq("receiver", loginUserId)
                .and(p1 -> p1
                        .eq("type", NotifyType.COMMENT.getType())
                        .or(p2 -> p2.eq("type", NotifyType.REPLY.getType()))
                )
                .list(page);
        //文章ID
        List<CommentMsgDTO> commentMsgs = NotifyMsgConverter.INSTANCE.toCommentMsgs(msgs);
        buildCommentMsgUser(commentMsgs);
        buildArticleTitle(commentMsgs);
        return PageData.<CommentMsgDTO>builder()
                .pageNum(page.getCurrent())
                .total(page.getTotal())
                .data(commentMsgs)
                .build();
    }

    public PageData<FollowMsgDTO> listFollowingMsg(PageParam param) {
        Page<NotifyMsg> page = param.toPage();
        int loginUserId = LoginUserContext.getLoginUserId();
        //新增粉丝
        List<NotifyMsg> msgs = ChainWrappers.queryChain(getBaseMapper())
                .eq("receiver", loginUserId)
                .eq("type", NotifyType.FOLLOW.getType())
                .orderByDesc("id")
                .list(page);
        List<Integer> userIds = ListMapperHandler.listTo(msgs, NotifyMsg::getSender);
        List<UserDTO> users = userService.getAuthorInfoByIds(userIds, UserOptionsBuilder.justInfo());
        List<FollowMsgDTO> followMsgs = NotifyMsgConverter.INSTANCE.toFollowMsgs(msgs);
        ListMapperHandler.doAssemble(followMsgs, FollowMsgDTO::getUserId, users,
                UserDTO::getUserId, FollowMsgDTO::setUser);
        return PageData.<FollowMsgDTO>builder()
                .pageNum(page.getCurrent())
                .total(page.getTotal())
                .data(followMsgs)
                .build();
    }

    public PageData<DiggMsgDTO> listLikeAndCollectMsg(PageParam param) {
        Page<NotifyMsg> page = param.toPage();
        int loginUserId = LoginUserContext.getLoginUserId();
        //点赞和收藏消息
        List<NotifyMsg> msgs = ChainWrappers.queryChain(getBaseMapper())
                .eq("receiver", loginUserId)
                .and(p1 -> p1.eq("type", NotifyType.ARTICLE_LIKE.getMsg())
                        .or(p2 -> p2.eq("type", NotifyType.COMMENT_LIKE.getType()))
                        .or(p3 -> p3.eq("type", NotifyType.COLLECT)))
                .orderByDesc("id")
                .list(page);
        List<DiggMsgDTO> dtos = NotifyMsgConverter.INSTANCE.toDiggMsgs(msgs);

        buildDiggMsgUser(dtos);
        buildDiggArticle(dtos);
        buildCommentLike(dtos);
        return PageData.<DiggMsgDTO>builder()
                .pageNum(page.getCurrent())
                .total(page.getTotal())
                .data(dtos)
                .build();
    }

    public PageData<ArticleMsgDTO> listArticleMsg(PageParam param) {
        Page<NotifyMsg> page = param.toPage();
        int loginUserId = LoginUserContext.getLoginUserId();
        List<NotifyMsg> msgs = ChainWrappers.queryChain(getBaseMapper())
                .eq("receiver", loginUserId)
                .eq("type", NotifyType.PUBLISH_ARTICLE.getType())
                .list(page);
        List<ArticleMsgDTO> dtos = NotifyMsgConverter.INSTANCE.toArticleMsgDTOs(msgs);
        buildArticleTitle(dtos, ArticleMsgDTO::getArticleId, ArticleMsgDTO::setTitle);
        List<Integer> userIds = ListMapperHandler.listTo(dtos, ArticleMsgDTO::getUserId);
        List<UserDTO> users = userService.getAuthorInfoByIds(userIds, UserOptionsBuilder.justInfo());
        ListMapperHandler.doAssemble(dtos, ArticleMsgDTO::getUserId, users, UserDTO::getUserId, ArticleMsgDTO::setUser);
        return PageData.<ArticleMsgDTO>builder()
                .pageNum(page.getCurrent())
                .total(page.getTotal())
                .data(dtos)
                .build();
    }

    private void buildCommentMsgUser(List<CommentMsgDTO> dtos) {
        List<Integer> userIds = ListMapperHandler.listTo(dtos, CommentMsgDTO::getUserId);
        List<UserDTO> users = userService.getAuthorInfoByIds(userIds, UserOptionsBuilder.justInfo());
        ListMapperHandler.doAssemble(dtos, CommentMsgDTO::getUserId, users, UserDTO::getUserId, CommentMsgDTO::setUser);
    }

    private void buildDiggMsgUser(List<DiggMsgDTO> dtos) {
        List<Integer> userIds = ListMapperHandler.listTo(dtos, DiggMsgDTO::getUserId);
        List<UserDTO> users = userService.getAuthorInfoByIds(userIds, UserOptionsBuilder.justInfo());
        ListMapperHandler.doAssemble(dtos, DiggMsgDTO::getUserId, users,
                UserDTO::getUserId, DiggMsgDTO::setUser);
    }


    private void buildDiggArticle(List<DiggMsgDTO> dtos) {
        List<DiggMsgDTO> list = dtos.stream().filter(dto -> !isComment(dto)).toList();
        buildArticleTitle(list, DiggMsgDTO::getItemId, DiggMsgDTO::setTitle);
    }

    private boolean isComment(DiggMsgDTO dto) {
        return NotifyType.COMMENT_LIKE.getType() == dto.getType();
    }


    private void buildCommentLike(List<DiggMsgDTO> dtos) {
        List<DiggMsgDTO> commentLike = dtos.stream().filter(this::isComment).toList();
        List<Integer> commentIds = ListMapperHandler.listTo(commentLike, DiggMsgDTO::getItemId);
        List<Comment> comments = commentRepository.listCommentById(commentIds);
        //评论信息
        ListMapperHandler.doAssemble(commentLike, DiggMsgDTO::getItemId, comments, Comment::getId, (d, c) -> {
            d.setContent(c.getContent());
        });
        final Map<Integer, Comment> commentMap = ListMapperHandler.listToMap(comments, Comment::getId);
        List<Integer> articleIds = ListMapperHandler.listTo(comments, Comment::getArticleId);
        List<ArticleInfo> articles = articleService.getArticleInfoByIds(articleIds);
        ListMapperHandler.doAssemble(commentLike,
                (c) -> commentMap.get(c.getItemId()).getArticleId(),
                articles,
                ArticleInfo::getId,
                (c, a) -> c.setTitle(a.getTitle())
        );

    }

    private void buildArticleTitle(List<CommentMsgDTO> commentMsgs) {
        buildArticleTitle(commentMsgs, CommentMsgDTO::getArticleId, CommentMsgDTO::setTitle);
    }

    private <T, R> void buildArticleTitle(List<T> dtos, Function<T, Integer> getter, BiConsumer<T, String> consumer) {
        List<Integer> articleIds = ListMapperHandler.listTo(dtos, getter);
        List<ArticleInfo> articles = articleService.getArticleInfoByIds(articleIds);
        ListMapperHandler.doAssemble(dtos, getter, articles, ArticleInfo::getId, (c, a) -> {
            String title = a.getTitle();
            consumer.accept(c, title);
        });
    }
}
