package top.wang3.hami.core.service.comment.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.converter.CommentConverter;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.builder.UserOptionsBuilder;
import top.wang3.hami.common.dto.comment.*;
import top.wang3.hami.common.dto.interact.LikeType;
import top.wang3.hami.common.dto.user.UserDTO;
import top.wang3.hami.common.message.CommentDeletedRabbitMessage;
import top.wang3.hami.common.message.CommentRabbitMessage;
import top.wang3.hami.common.message.RabbitMessage;
import top.wang3.hami.common.message.ReplyRabbitMessage;
import top.wang3.hami.common.model.Comment;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.exception.HamiServiceException;
import top.wang3.hami.core.service.article.repository.ArticleRepository;
import top.wang3.hami.core.service.comment.CommentService;
import top.wang3.hami.core.service.comment.repository.CommentRepository;
import top.wang3.hami.core.service.interact.LikeService;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.security.context.IpContext;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.*;
import java.util.function.Function;


@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final ArticleRepository articleRepository;
    private final LikeService likeService;
    private final RabbitMessagePublisher rabbitMessagePublisher;

    @Resource
    TransactionTemplate transactionTemplate;

    @CostLog
    @Override
    public PageData<CommentDTO> listComment(CommentPageParam commentPageParam) {
        Page<Comment> page = commentPageParam.toPage();
        Integer articleId = commentPageParam.getArticleId();
        Assert.isTrue(articleId != null && articleId > 0, "invalid article_id");
        Integer sort = commentPageParam.getSort();
        //获取文章评论
        List<Comment> comments = commentRepository.listComment(page, articleId, sort);
        List<CommentDTO> dtos = CommentConverter.INSTANCE.toCommentDTOList(comments);
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
//        page.setSearchCount(false);
        List<Comment> comments = commentRepository.listReply(page, articleId, rootId);
        List<CommentDTO> dtos = CommentConverter.INSTANCE.toCommentDTOList(comments);
        buildUserInfo(dtos);
        buildHasLiked(dtos);
        return PageData.<CommentDTO>builder()
                .pageNum(page.getCurrent())
                .total(page.getTotal())
                .data(dtos)
                .build();
    }

    @Override
    public ReplyDTO listIndexReply(Integer articleId, Integer rootId) {
        Reply reply = commentRepository.listIndexReply(articleId, rootId);
        ReplyDTO dto = new ReplyDTO();
        dto.setTotal(reply.getTotal());
        dto.setList(CommentConverter.INSTANCE.toCommentDTOList(reply.getComments()));
        return dto;
    }

    @Override
    public Comment publishComment(CommentParam param) {
        //发表评论
        //todo 敏感词过滤
        return publishComment(param, false);
    }

    @Override
    public Comment publishReply(CommentParam param) {
        //回复
        return publishComment(param, true);
    }

    @Override
    public boolean deleteComment(Integer id) {
        //删除评论
        Comment comment = commentRepository.getById(id);
        Integer articleId = comment.getArticleId();
        Integer owner = articleRepository.getArticleAuthor(articleId);
        int userId = LoginUserContext.getLoginUserId();
        if (userId != owner || userId != comment.getUserId()) {
            //自己发表的可以删除
            //评论区拥有者可以删除
            return false;
        }
        Integer deleteCount = transactionTemplate.execute(status -> {
            return commentRepository.deleteComment(id);
        });
        if (deleteCount != null && deleteCount > 0) {
            //评论删除消息
            var message = new CommentDeletedRabbitMessage(comment.getArticleId(), deleteCount);
            rabbitMessagePublisher.publishMsg(message);
            return true;
        }
        return false;
    }

    private Comment publishComment(CommentParam param, boolean reply) {
        Comment comment = buildComment(param, reply);
        //评论
        Boolean success = transactionTemplate.execute(status -> {
            return commentRepository.save(comment);
        });
        if (Boolean.FALSE.equals(success)) {
            return null;
        }
        RabbitMessage message;
        Integer author = articleRepository.getArticleAuthor(param.getArticleId());
        if (reply) {
            message = new ReplyRabbitMessage(comment, author);
        } else {
            message = new CommentRabbitMessage(comment, author);
        }
        rabbitMessagePublisher.publishMsg(message);
        return comment;
    }

    private Comment buildComment(CommentParam param, boolean reply) {
        int loginUserId = LoginUserContext.getLoginUserId();
        Integer author = articleRepository.getArticleAuthor(param.getArticleId());
        if (author == null) {
            throw new HamiServiceException("文章不存在");
        }
        Comment comment = CommentConverter.INSTANCE.toComment(param);
        comment.setUserId(loginUserId); //评论用户
        comment.setIpInfo(IpContext.getIpInfo()); //IP信息
        if (reply) {
            return buildReply(comment, param);
        }
        return comment;
    }

    private Comment buildReply(Comment comment, CommentParam param) {
        //回复, parentId和rootId必须都大于0
        //check rootId;
        //check parentId;
        //一级评论 rootId和parentId都为0
        //二级评论 rootId = parentId != 0
        Integer rootId = param.getRootId();
        Integer parentId = param.getParentId();
        Assert.isTrue(rootId != null && rootId != 0, "rootId can not be null or zero");
        Assert.isTrue(parentId != null && parentId != 0, "parentId can not be null or zero");
        Comment parentComment = commentRepository.getById(parentId);
        if (parentComment == null) {
            throw new HamiServiceException("参数错误");
        } else if (Objects.equals(rootId, parentId) &&
                parentComment.getRootId() != 0) {
            //二级评论 回复的是根评论
            //根评论的rootId应该为0
            throw new HamiServiceException("参数错误");
        } else if (parentComment.getRootId() != 0 &&
                !Objects.equals(parentComment.getRootId(), rootId)) {
            //三级以上的评论, 必须在同一个根评论下
            throw new HamiServiceException("参数错误");
        }
        comment.setRootId(rootId);
        comment.setParentId(parentId);
        //回复的是父评论的userId
        comment.setReplyTo(parentComment.getUserId());
        return comment;
    }

    private void buildIndexReply(List<CommentDTO> dtos) {
        dtos.forEach(dto -> {
            ReplyDTO reply = this.listIndexReply(dto.getArticleId(), dto.getId());
            dto.setReply(reply);
        });
    }

    private void buildUserInfo(List<CommentDTO> dtos) {
        Collection<Integer> userIds = getUserId(dtos);
        var builder = new UserOptionsBuilder()
                .noStat()
                .noFollowState();
        Collection<UserDTO> users = userService.listAuthorInfoById(userIds, builder);
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
        Integer reply = dto.getReplyTo();
        if (reply != null) {
            dto.setReplyUser(users.get(reply));
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
        Map<Integer, Boolean> liked = likeService.hasLiked(loginUserId, items, LikeType.COMMENT);
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
        getUserId(set, comments);
        return set;
    }

    private void getUserId(Collection<Integer> data, List<CommentDTO> comments) {
        if (CollectionUtils.isEmpty(comments)) {
            return;
        }
        for (CommentDTO dto : comments) {
            data.add(dto.getUserId());
            if (dto.getReplyTo() != null && dto.getReplyTo() != 0) {
                data.add(dto.getReplyTo());
            }
            ReplyDTO reply = dto.getReply();
            if (reply != null) {
                getUserId(data, reply.getList());
            }
        }
    }
}
