package top.wang3.hami.core.service.article.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import top.wang3.hami.common.constant.ContentState;
import top.wang3.hami.common.converter.ArticleConverter;
import top.wang3.hami.common.dto.ArticleDraftPageParam;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.dto.article.ArticleDraftParam;
import top.wang3.hami.common.message.ArticleRabbitMessage;
import top.wang3.hami.common.message.ReviewMessage;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.model.ArticleDraft;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.util.Predicates;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.exception.HamiServiceException;
import top.wang3.hami.core.mapper.ArticleStatMapper;
import top.wang3.hami.core.service.article.ArticleDraftService;
import top.wang3.hami.core.service.article.CategoryService;
import top.wang3.hami.core.service.article.TagService;
import top.wang3.hami.core.service.article.repository.ArticleDraftRepository;
import top.wang3.hami.core.service.article.repository.ArticleRepository;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleDraftServiceImpl implements ArticleDraftService {

    private final ArticleStatMapper articleStatMapper;

    private final CategoryService categoryService;
    private final TagService tagService;
    private final RabbitMessagePublisher rabbitMessagePublisher;

    private final ArticleDraftRepository articleDraftRepository;
    private final ArticleRepository articleRepository;

    @Resource
    Validator validator;

    @Resource
    TransactionTemplate transactionTemplate;

    @Override
    public PageData<ArticleDraft> listDraft(ArticleDraftPageParam param) {
        Page<ArticleDraft> page = param.toPage();
        Integer userId = param.getUserId();
        if (userId != null) {
            List<ArticleDraft> drafts = articleDraftRepository.getDraftByPage(page, userId, param.getState());
            return PageData.build(page, drafts);
        }
        List<ArticleDraft> drafts = articleDraftRepository.getDraftByPage(page, param.getState());
        return PageData.build(page, drafts);
    }

    @Override
    public PageData<ArticleDraft> listDraftByPage(PageParam param, byte state) {
        int loginUserId = LoginUserContext.getLoginUserId();
        Page<ArticleDraft> page = param.toPage();
        List<ArticleDraft> drafts = articleDraftRepository.getDraftByPage(page, loginUserId, state);
        return PageData.build(page, drafts);
    }

    @Override
    public ArticleDraft getArticleDraftById(long draftId) {
        int loginUserId = LoginUserContext.getLoginUserId();
        return articleDraftRepository.getDraftById(draftId, loginUserId);
    }

    /**
     * 创建文章草稿
     *
     * @param param 参数
     * @return ArticleDraft
     */
    @Override
    public ArticleDraft createDraft(ArticleDraftParam param) {
        if (param == null || param.getId() != null) {
            return null;
        }
        ArticleDraft draft = ArticleConverter.INSTANCE.toArticleDraft(param);
        // fix: Field 'user_id' doesn't have a default value
        int userId = LoginUserContext.getLoginUserId();
        draft.setUserId(userId);
        draft.setState(ContentState.ORIGIN_DRAFT.value);
        boolean saved = articleDraftRepository.createDraft(draft);
        return saved ? draft : null;
    }

    @Override
    public ArticleDraft updateDraft(ArticleDraftParam param) {
        // 更新草稿
        ArticleDraft draft = ArticleConverter.INSTANCE.toArticleDraft(param);
        if (draft.getId() == null) {
            throw new HamiServiceException("参数错误");
        }
        int loginUserId = LoginUserContext.getLoginUserId();
        // 获取旧的
        ArticleDraft oldDraft = articleDraftRepository.getDraftById(draft.getId(), loginUserId);
        if (oldDraft == null) {
            throw new HamiServiceException("参数错误");
        }
        draft.setVersion(oldDraft.getVersion());
        boolean success = articleDraftRepository.updateDraft(draft);
        return success ? draft : null;
    }

    @Override
    public void publishOrUpdate(Long draftId) {
        // 用户点击发表或者更新文章按钮
        // 1.检查参数, 除了文章ID, 其他都不能为空
        int loginUserId = LoginUserContext.getLoginUserId();
        // 2.获取草稿
        final ArticleDraft draft = articleDraftRepository.getDraftById(draftId, loginUserId);
        // 3.校验draft
        checkDraft(draft);
        Byte state = draft.getState();
        // 4.检查状态
        if (ContentState.REVIEW.value.equals(state)) {
            // 已经是审核状态
            return;
        }
        // 5.更新草稿状态为审核
        boolean success = articleDraftRepository.updateDraftState(draftId, ContentState.REVIEW, draft.getVersion());
        if (!success) {
            throw new HamiServiceException("更新草稿状态失败, 请联系管理员");
        }
        log.info("update draft state success, draftId: {}", draftId);
        ReviewMessage reviewMessage = new ReviewMessage(draftId);
        rabbitMessagePublisher.publishMsg(reviewMessage);
    }

    /**
     * @param draftId 草稿ID
     */
    @Override
    public void publishArticle(Long draftId) {
        // todo 分布式锁保证并发更新正确性 或者 消息队列的顺序消息
        // todo 同一个草稿的审核状态消息发送到同一个分区, 避免上一个消息未消费完,就消费了下一个消息
        // 1.获取草稿
        final ArticleDraft draft = articleDraftRepository.getById(draftId);
        // 校验draft
        checkDraft(draft);
        // 2.转化为文章实体
        Article article = ArticleConverter.INSTANCE.toArticle(draft);
        ArticleRabbitMessage message = new ArticleRabbitMessage(
            null,
            article.getId(),
            article.getUserId()
        );
        message.setCateId(article.getCategoryId());
        Boolean success = transactionTemplate.execute(status -> {
            if (article.getId() == null && handleInsert(article, draft, draft.getUserId())) {
                // 3.插入文章, 成功发布文章发表消息
                message.setArticleId(article.getId());
                message.setType(ArticleRabbitMessage.Type.PUBLISH);
                return true;
            } else if (article.getId() != null && handleUpdate(article)) {
                // 3.更新文章, 成功发布文章更新消息
                message.setType(ArticleRabbitMessage.Type.UPDATE);
                return true;
            } else {
                // 3.插入或者更新失败
                status.setRollbackOnly();
                return false;
            }
        });
        if (Boolean.TRUE.equals(success)) {
            rabbitMessagePublisher.publishMsg(message);
        }
    }

    @Override
    public boolean deleteOriginDraft(long draftId) {
        // 刪除草稿
        int userId = LoginUserContext.getLoginUserId();
        return articleDraftRepository.deleteOriginDraft(draftId, userId);
    }

    @Override
    public boolean deleteArticle(long draftId) {
        // 删除文章
        int loginUserId = LoginUserContext.getLoginUserId();
        ArticleDraft draft = articleDraftRepository.getDraftById(draftId, loginUserId);
        Integer articleId = draft.getArticleId();
        if (ContentState.ORIGIN_DRAFT.value.equals(draft.getState())) {
            throw new HamiServiceException("草稿状态异常");
        }
        Boolean success = transactionTemplate.execute(status -> {
            if (articleId == null) {
                // 没有发表过
                return articleDraftRepository.deleteDraft(draftId, loginUserId);
            } else if (articleRepository.deleteArticle(articleId, loginUserId)
                       && articleDraftRepository.deleteDraft(draftId, loginUserId)) {
                // 同时删除草稿
                return true;
            } else {
                status.setRollbackOnly();
                return false;
            }
        });
        if (articleId != null && Boolean.TRUE.equals(success)) {
            // actually deleted
            var message = new ArticleRabbitMessage(ArticleRabbitMessage.Type.DELETE,
                articleId, loginUserId, loginUserId);
            rabbitMessagePublisher.publishMsg(message);
            return true;
        }
        return success;
    }

    private boolean handleInsert(Article article, ArticleDraft draft, Integer userId) {
        // 插入, 文章ID会回显
        boolean success = articleRepository.saveArticle(article);
        // 设置草稿中的文章ID
        draft.setArticleId(article.getId());
        final Integer articleId = article.getId();
        return Predicates.check(success)
            .then(() -> {
                // 初始化文章数据
                int rows = articleStatMapper.insert(new ArticleStat(articleId, userId));
                return rows == 1;
            })
            .then(() -> {
                // 更新文章草稿
                ArticleDraft newDraft = new ArticleDraft(draft.getId(), articleId, null, draft.getVersion());
                return articleDraftRepository.updateDraft(newDraft);
            }).get();
    }

    private boolean handleUpdate(Article article) {
        // 直接更新即可, 不要在更新标签啦
        article.setUserId(null);
        return articleRepository.updateArticle(article);
    }

    @Override
    public void checkDraft(ArticleDraft draft) {
        if (draft == null) {
            throw new HamiServiceException("草稿不存在");
        }
        validator.validate(draft);
        // 校验分类
        // assert cateId != null
        if (categoryService.getCategoryById(draft.getCategoryId()) == null) {
            throw new HamiServiceException("分类不存在");
        }
        // 校验标签 若有个tagId不存在抛出错误
        List<Integer> tagIds = draft.getTagIds();
        for (Integer tagId : tagIds) {
            if (tagId == null || tagService.getTagById(tagId) == null) {
                throw new HamiServiceException("标签不存在");
            }
        }
    }

}
