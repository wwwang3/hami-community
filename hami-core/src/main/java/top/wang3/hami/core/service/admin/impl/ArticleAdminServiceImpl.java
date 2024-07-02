package top.wang3.hami.core.service.admin.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.ContentState;
import top.wang3.hami.common.dto.ArticleDraftPageParam;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.builder.UserOptionsBuilder;
import top.wang3.hami.common.message.ReviewFailedMessage;
import top.wang3.hami.common.message.ReviewSuccessMessage;
import top.wang3.hami.common.model.ArticleDraft;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.vo.article.ArticleDraftVo;
import top.wang3.hami.common.vo.user.UserVo;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.exception.HamiServiceException;
import top.wang3.hami.core.service.admin.ArticleAdminService;
import top.wang3.hami.core.service.article.ArticleDraftService;
import top.wang3.hami.core.service.article.repository.ArticleDraftRepository;
import top.wang3.hami.core.service.user.UserService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleAdminServiceImpl implements ArticleAdminService {

    private final ArticleDraftService articleDraftService;
    private final ArticleDraftRepository articleDraftRepository;
    private final RabbitMessagePublisher rabbitMessagePublisher;
    private final UserService userService;

    @Override
    public void reviewSuccess(ArticleDraft draft) {
        // 检查草稿
        articleDraftService.checkDraft(draft);
        // 更新状态
        boolean success = articleDraftRepository.updateDraftState(
            draft.getId(),
            ContentState.NORMAL,
            draft.getVersion()
        );
        if (!success) {
            throw new HamiServiceException("更新草稿状态失败");
        }
        ReviewSuccessMessage message = new ReviewSuccessMessage(draft.getId(), draft.getUserId(), draft.getTitle());
        rabbitMessagePublisher.publishMsg(message);
    }

    @Override
    public void reviewFailed(ArticleDraft draft, String msg) {
        // 更新状态
        boolean success = articleDraftRepository.updateDraftState(
            draft.getId(),
            ContentState.REVIEW_FAILED,
            draft.getVersion()
        );
        if (!success) {
            throw new HamiServiceException("更新草稿状态失败");
        }
        ReviewFailedMessage message = new ReviewFailedMessage(draft.getId(), draft.getUserId(), draft.getTitle(), msg);
        rabbitMessagePublisher.publishMsg(message);
    }

    @Override
    public PageData<ArticleDraftVo> listReviewDraft(ArticleDraftPageParam param) {
        Page<ArticleDraft> page = param.toPage();
        List<Byte> states = List.of(ContentState.REVIEW.value, ContentState.REVIEW_FAILED.value, ContentState.NORMAL.value);
        List<ArticleDraft> drafts = articleDraftRepository.getDraftByPage(page, states);
        List<Integer> userIds = ListMapperHandler.listTo(drafts, ArticleDraft::getUserId);
        List<UserVo> userVos = userService.listAuthorById(userIds, UserOptionsBuilder.justInfo());
        ArrayList<ArticleDraftVo> vos = new ArrayList<>(drafts.size());
        ListMapperHandler.doAssemble(drafts, ArticleDraft::getUserId, userVos, UserVo::getUserId, (a, b) -> {
            ArticleDraftVo vo = new ArticleDraftVo();
            vo.setDraft(a);
            vo.setUser(b);
            vos.add(vo);
        });
        return PageData.build(page, vos);
    }

    @Override
    public void reviewArticle(ArticleDraft draft, String msg) {
        log.info("draft: {}", draft);
        Byte state = draft.getState();
        // 检查草稿
        articleDraftService.checkDraft(draft);
        if (state == null || ContentState.ORIGIN_DRAFT.value.equals(state)) {
            // 不能更新为草稿状态
            throw new HamiServiceException("状态异常");
        }
        // 更新状态
        ContentState contentState = ContentState.values()[state];
        boolean success = articleDraftRepository.updateDraftState(
            draft.getId(),
            contentState,
            draft.getVersion()
        );
        if (!success) {
            throw new HamiServiceException("更新草稿状态失败");
        }
        if (contentState == ContentState.NORMAL) {
            ReviewSuccessMessage message = new ReviewSuccessMessage(draft.getId(), draft.getUserId(), draft.getTitle());
            rabbitMessagePublisher.publishMsg(message);
        } else if (contentState == ContentState.REVIEW_FAILED) {
            ReviewFailedMessage message = new ReviewFailedMessage(draft.getId(), draft.getUserId(), draft.getTitle(), msg);
            rabbitMessagePublisher.publishMsg(message);
        }
    }

}
