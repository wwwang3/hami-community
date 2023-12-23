package top.wang3.hami.core.service.article.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.converter.ArticleConverter;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.dto.article.ArticleDraftDTO;
import top.wang3.hami.common.dto.article.ArticleDraftParam;
import top.wang3.hami.common.message.ArticleRabbitMessage;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.model.ArticleDraft;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.model.Tag;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.exception.HamiServiceException;
import top.wang3.hami.core.mapper.ArticleStatMapper;
import top.wang3.hami.core.service.article.*;
import top.wang3.hami.core.service.article.repository.ArticleDraftRepository;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleDraftServiceImpl implements ArticleDraftService {

    private final ArticleService articleService;
    private final ArticleTagService articleTagService;
    private final ArticleStatMapper articleStatMapper;

    private final CategoryService categoryService;
    private final TagService tagService;
    private final RabbitMessagePublisher rabbitMessagePublisher;

    private final ArticleDraftRepository articleDraftRepository;

    @Resource
    Validator validator;

    @Resource
    TransactionTemplate transactionTemplate;


    @Override
    public PageData<ArticleDraftDTO> getArticleDrafts(PageParam param, byte state) {
        int loginUserId = LoginUserContext.getLoginUserId();
        Page<ArticleDraft> page = param.toPage();
        List<ArticleDraft> drafts = articleDraftRepository.getDraftsByPage(page, loginUserId, state);
        List<ArticleDraftDTO> dtos = buildDrafts(drafts);
        return PageData.<ArticleDraftDTO>builder()
                .pageNum(page.getCurrent())
                .total(page.getTotal())
                .data(dtos)
                .build();
    }

    @Override
    public ArticleDraftDTO getArticleDraftById(long draftId) {
        int loginUserId = LoginUserContext.getLoginUserId();
        ArticleDraft draft = articleDraftRepository.getDraftById(draftId, loginUserId);
        if (draft == null) {
            throw new HamiServiceException("草稿不存在");
        }
        log.debug("draft-{}", draft);
        List<Integer> tagsIds = draft.getTagIds();
        List<Tag> tags = tagService.getTagsByIds(tagsIds);
        return ArticleConverter.INSTANCE.toDraftDTO(draft, tags);
    }

    /**
     * 创建文章草稿
     * @param param 参数
     * @return ArticleDraft
     */
    @Override
    public ArticleDraft createDraft(ArticleDraftParam param) {
        if (param == null || param.getId() != null) {
            return null;
        }
        ArticleDraft draft = ArticleConverter.INSTANCE.toArticleDraft(param);
        //fix: Field 'user_id' doesn't have a default value
        int userId = LoginUserContext.getLoginUserId();
        draft.setUserId(userId);
        draft.setState(Constants.ZERO);
        boolean saved = articleDraftRepository.saveDraft(draft);
        return saved ? draft : null;
    }

    @Override
    public ArticleDraft updateDraft(ArticleDraftParam param) {
        //更新草稿
        ArticleDraft draft = ArticleConverter.INSTANCE.toArticleDraft(param);
        if (draft.getId() == null) {
            throw new HamiServiceException("参数错误");
        }
        int loginUserId = LoginUserContext.getLoginUserId();
        //获取旧的
        ArticleDraft oldDraft = articleDraftRepository.getDraftById(draft.getId(),  loginUserId);
        draft.setVersion(oldDraft.getVersion());
        boolean success = articleDraftRepository.updateDraft(draft);
        return success ? draft : null;
    }

    /**
     * todo 文章审核
     *
     * @param draftId 草稿ID
     * @return ArticleDraft
     */
    @Override
    public ArticleDraft publishArticle(Long draftId) {
        //检查参数, 文章ID, 其他都不能为空
        int loginUserId = LoginUserContext.getLoginUserId();
        //获取草稿
        final ArticleDraft draft = articleDraftRepository.getDraftById(draftId, loginUserId);
        //校验draft
        checkDraft(draft);
        //文章
        Article article = ArticleConverter.INSTANCE.toArticle(draft);
        Boolean success = transactionTemplate.execute(status -> {
            //发表文章
            boolean success1;
            if (article.getId() == null) {
                //插入
                success1 = handleInsert(article, draft, loginUserId);
                if (success1) {
                    ArticleRabbitMessage message = new ArticleRabbitMessage(ArticleRabbitMessage.Type.PUBLISH,
                            article.getId(), article.getUserId());
                    message.setCateId(article.getCategoryId());
                    rabbitMessagePublisher.publishMsg(message);
                    return true;
                }
            } else {
                //更新
                success1 = handleUpdate(article, draft);
            }
            if (!success1) {
                status.setRollbackOnly();
                return false;
            }
            return true;
        });
        return Boolean.TRUE.equals(success) ? draft : null;
    }

    @Override
    public boolean deleteDraft(long draftId) {
        //刪除草稿
        int userId = LoginUserContext.getLoginUserId();
        return articleDraftRepository.deleteDraftById(draftId, userId);
    }

    @Override
    public boolean deleteArticle(int articleId) {
        //删除文章
        int userId = LoginUserContext.getLoginUserId();
        Boolean success = transactionTemplate.execute(status -> {
            //删除文章
            boolean deleted = articleService.deleteByArticleId(articleId, userId);
            if (!deleted) {
                return false;
            }
            //删除草稿
            boolean removed = articleDraftRepository.deleteDraftByArticleId(articleId, userId);
            if (!removed) {
                status.setRollbackOnly();
                return false;
            }
            return true;
        });
        if (Boolean.TRUE.equals(success)) {
            var message = new ArticleRabbitMessage(ArticleRabbitMessage.Type.DELETE,
                    articleId, userId, userId);
            rabbitMessagePublisher.publishMsg(message);
            return true;
        }
        return false;
    }

    private boolean handleInsert(Article article, ArticleDraft draft, Integer loginUserId) {
        //插入
        boolean success1 = articleService.saveArticle(article);
        draft.setArticleId(article.getId());
        draft.setState(Constants.ONE);
        if (success1) {
            // 插入文章标签
            articleTagService.saveTags(article.getId(), draft.getTagIds());
            // 初始化文章数据
            articleStatMapper.insert(new ArticleStat(article.getId(), loginUserId));
            ArticleDraft newDraft = new ArticleDraft(draft.getId(), article.getId(), Constants.ONE, draft.getVersion());
            // 更新文章草稿
            return articleDraftRepository.updateDraft(newDraft);
        }
        return false;
    }

    private boolean handleUpdate(Article article, ArticleDraft draft) {
        if (articleService.updateArticle(article) &&
                articleTagService.updateTags(article.getId(), draft.getTagIds())) {
            ArticleRabbitMessage message = new ArticleRabbitMessage(ArticleRabbitMessage.Type.UPDATE,
                    article.getId(), article.getUserId());
            rabbitMessagePublisher.publishMsg(message);
            message.setCateId(article.getCategoryId());
            return true;
        }
        return false;
    }

    private void checkDraft(ArticleDraft draft) {
        validator.validate(draft);
        //校验分类
        if (categoryService.getCategoryDTOById(draft.getCategoryId()) == null) {
            throw new HamiServiceException("请选择一个分类");
        }
        ///校验标签 若有个tagId不存在抛出错误
        List<Integer> tagIds = draft.getTagIds();
        if (!tagService.checkTags(tagIds)) {
            throw new HamiServiceException("标签参数错误");
        }
    }

    private List<ArticleDraftDTO> buildDrafts(List<ArticleDraft> drafts) {
        return drafts.stream()
                .map(draft -> {
                    List<Integer> tagsIds = draft.getTagIds();
                    List<Tag> tags = tagService.getTagsByIds(tagsIds);
                    return ArticleConverter.INSTANCE.toDraftDTO(draft, tags);
                }).toList();
    }
}
