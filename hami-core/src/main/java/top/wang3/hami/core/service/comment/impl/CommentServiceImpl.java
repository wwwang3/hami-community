package top.wang3.hami.core.service.comment.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.converter.CommentConverter;
import top.wang3.hami.common.dto.*;
import top.wang3.hami.common.dto.request.CommentPageParam;
import top.wang3.hami.common.dto.request.CommentParam;
import top.wang3.hami.common.model.Comment;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.repository.CommentRepository;
import top.wang3.hami.core.service.comment.CommentService;
import top.wang3.hami.core.service.interact.UserInteractService;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.*;
import java.util.function.Function;


@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final UserInteractService userInteractService;

    @CostLog
    @Override
    public PageData<CommentDTO> listComment(CommentPageParam commentPageParam) {
        Page<Comment> page = commentPageParam.toPage();
        Integer articleId = commentPageParam.getArticleId();
        Assert.isTrue(articleId != null && articleId > 0, "invalid article_id");
        Integer sort = commentPageParam.getSort();
        //获取文章评论
        List<Comment> comments = commentRepository.listComment(page, articleId, sort);
        List<CommentDTO> dtos = CommentConverter.INSTANCE.toCommentDTO(comments);
        //获取五条回复
        buildIndexReply(dtos);
        //用户信息
        buildUserInfo(dtos);
        //是否点赞评论
        buildHasLiked(dtos);
        return PageData.<CommentDTO>builder()
                .pageNum(page.getCurrent())
                .total(page.getTotal())
                .data(dtos)
                .build();
    }


    @Override
    public PageData<CommentDTO> listReply(CommentPageParam commentPageParam) {
        //获取回复
        Integer articleId = commentPageParam.getArticleId();
        Integer rootId = commentPageParam.getRootId();
        if (rootId == null || rootId <= 0) {
            return PageData.empty();
        }
        Page<Comment> page = commentPageParam.toPage();
        page.setSearchCount(false);
        List<Comment> comments = commentRepository.listReply(page, articleId, rootId);
        List<CommentDTO> dtos = CommentConverter.INSTANCE.toCommentDTO(comments);
        buildUserInfo(dtos, true);
        buildHasLiked(dtos);
        return PageData.<CommentDTO>builder()
                .pageNum(page.getCurrent())
                .total(page.getTotal())
                .data(dtos)
                .build();
    }

    @Override
    public ReplyDTO listIndexReply(Integer rootId) {
        Reply reply = commentRepository.listIndexReply(rootId);
        ReplyDTO dto = new ReplyDTO();
        dto.setTotal(reply.getTotal());
        dto.setList(CommentConverter.INSTANCE.toCommentDTO(reply.getComments()));
        return dto;
    }

    @Override
    public CommentDTO publishComment(CommentParam param) {
        //发表评论
        int userId = LoginUserContext.getLoginUserId();

    }

    @Override
    public CommentDTO publishReply(CommentParam param) {
        return null;
    }

    @Override
    public int deleteComment(Integer id) {


    }

    private void buildIndexReply(List<CommentDTO> dtos) {
        dtos.forEach(dto -> {
            ReplyDTO reply = this.listIndexReply(dto.getId());
            dto.setReply(reply);
        });
    }

    private void buildUserInfo(List<CommentDTO> dtos) {
        Collection<Integer> set = getUserId(dtos);
        List<Integer> userIds = set.stream().toList();
        var builder = new UserService.OptionsBuilder()
                .noStat()
                .noFollowState();
        List<UserDTO> users = userService.getAuthorInfoByIds(userIds, builder);
        Map<Integer, UserDTO> map =
                ListMapperHandler.listToMap(users, UserDTO::getUserId, Function.identity());
        buildUserInfo(dtos, map);
    }

    private void buildUserInfo(List<CommentDTO> comments, Map<Integer, UserDTO> users) {
        for (CommentDTO comment : comments) {
            ReplyDTO reply = comment.getReply();
            setUserInfo(comment, users);
            if (reply != null && reply.getList() != null) {
                var list = reply.getList();
                for (CommentDTO dto : list) {
                    setUserInfo(dto, users);
                }
            }
        }
    }

    private void setUserInfo(CommentDTO dto, Map<Integer, UserDTO> users) {
        dto.setUser(users.get(dto.getUserId()));
        Integer reply = dto.getComment().getReplyTo();
        if (reply != null) {
            dto.setReplyTo(users.get(reply));
        }
    }

    private void buildHasLiked(List<CommentDTO> comments) {
        Integer loginUserId = LoginUserContext.getLoginUserIdDefaultNull();
        if (loginUserId == null) return;
        ArrayList<Integer> items = new ArrayList<>(128);
        for (CommentDTO comment : comments) {
            items.add(comment.getId());
            ReplyDTO reply = comment.getReply();
            if (reply != null && reply.getList() != null) {
                var list = reply.getList();
                for (CommentDTO dto : list) {
                    items.add(dto.getId());
                }
            }
        }
        Map<Integer, Boolean> liked = userInteractService.hasLiked(loginUserId, items,
                Constants.LIKE_TYPE_COMMENT);
        buildHasLiked(comments, liked);
    }

    private void buildHasLiked(List<CommentDTO> comments, Map<Integer, Boolean> liked) {
        if (comments == null || comments.isEmpty()) return;
        for (CommentDTO dto : comments) {
            dto.setLiked(liked.get(dto.getId()));
            ReplyDTO reply = dto.getReply();
            if (reply != null && reply.getList() != null) {
                List<CommentDTO> items = reply.getList();
                for (CommentDTO item : items) {
                    item.setLiked(liked.get(item.getId()));
                }
            }
        }

    }

    private Collection<Integer> getUserId(List<CommentDTO> comments) {
        Collection<Integer> set = new HashSet<>();
        for (CommentDTO dto : comments) {
            set.add(dto.getUserId());
            Comment comment = dto.getComment();
            if (comment.getReplyTo() != null) {
                set.add(comment.getReplyTo());
            }
            ReplyDTO reply = dto.getReply();
            if (reply != null && reply.getList() != null) {
                List<CommentDTO> list = reply.getList();
                for (CommentDTO r : list) {
                    set.add(r.getUserId());
                    Integer replyTo = r.getComment().getReplyTo();
                    if (replyTo != null) {
                        set.add(replyTo);
                    }
                }
            }
        }
        return set;
    }
}
