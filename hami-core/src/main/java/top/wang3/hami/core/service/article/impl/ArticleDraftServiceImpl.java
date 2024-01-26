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
import top.wang3.hami.common.dto.article.ArticleDraftParam;
import top.wang3.hami.common.message.ArticleRabbitMessage;
import top.wang3.hami.common.model.*;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.Predicates;
import top.wang3.hami.common.vo.article.ArticleDraftVo;
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
import java.util.Map;

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
    public PageData<ArticleDraftVo> listDraftByPage(PageParam param, byte state) {
        int loginUserId = LoginUserContext.getLoginUserId();
        Page<ArticleDraft> page = param.toPage();
        List<ArticleDraft> drafts = articleDraftRepository.getDraftsByPage(page, loginUserId, state);
        List<ArticleDraftVo> dtos = buildDrafts(drafts);
        return PageData.<ArticleDraftVo>builder()
                .current(page.getCurrent())
                .total(page.getTotal())
                .data(dtos)
                .build();
    }

    @Override
    public ArticleDraftVo getArticleDraftById(long draftId) {
        int loginUserId = LoginUserContext.getLoginUserId();
        ArticleDraft draft = articleDraftRepository.getDraftById(draftId, loginUserId);
        if (draft == null) return null;
        List<Integer> tagsIds = draft.getTagIds();
        List<Tag> tags = tagService.getTagsByIds(tagsIds);
        return ArticleConverter.INSTANCE.toDraftDTO(draft, tags);
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
        //fix: Field 'user_id' doesn't have a default value
        int userId = LoginUserContext.getLoginUserId();
        draft.setUserId(userId);
        draft.setState(Constants.ZERO);
        boolean saved = articleDraftRepository.saveDraft(draft);
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

    /**
     *
     * @param draftId 草稿ID
     * @return ArticleDraft
     */
    @Override
    public ArticleDraft publishArticle(Long draftId) {
        // 检查参数, 除了文章ID, 其他都不能为空
        int loginUserId = LoginUserContext.getLoginUserId();
        // 获取草稿
        final ArticleDraft draft = articleDraftRepository.getDraftById(draftId, loginUserId);
        // 校验draft
        checkDraft(draft);
        // 文章
        Article article = ArticleConverter.INSTANCE.toArticle(draft);
        Boolean success = transactionTemplate.execute(status -> {
            if (article.getId() == null && handleInsert(article, draft, loginUserId)) {
                // 插入文章, 成功发布文章发表消息
                ArticleRabbitMessage message = new ArticleRabbitMessage(ArticleRabbitMessage.Type.PUBLISH,
                        article.getId(), article.getUserId());
                message.setCateId(article.getCategoryId());
                rabbitMessagePublisher.publishMsg(message);
                return true;
            } else if (article.getId() != null && handleUpdate(article)){
                // 更新文章, 成功发布文章更新消息
                ArticleRabbitMessage message = new ArticleRabbitMessage(ArticleRabbitMessage.Type.UPDATE,
                        article.getId(), article.getUserId());
                rabbitMessagePublisher.publishMsg(message);
                message.setCateId(article.getCategoryId());
                return true;
            } else {
                // 插入或者更新失败
                status.setRollbackOnly();
                return false;
            }
        });
        return Boolean.TRUE.equals(success) ? draft : null;
    }

    @Override
    public boolean deleteDraft(long draftId) {
        // 刪除草稿
        int userId = LoginUserContext.getLoginUserId();
        return articleDraftRepository.deleteDraftById(draftId, userId);
    }

    @Override
    public boolean deleteArticle(int articleId) {
        // 删除文章
        int loginUserId = LoginUserContext.getLoginUserId();
        Boolean success = transactionTemplate.execute(status -> {
            // 同时删除草稿
            if (articleRepository.deleteArticle(articleId, loginUserId) &&
                articleDraftRepository.deleteDraftByArticleId(articleId, loginUserId)) {
                return true;
            } else {
                status.setRollbackOnly();
                return false;
            }
        });
        if (Boolean.TRUE.equals(success)) {
            var message = new ArticleRabbitMessage(ArticleRabbitMessage.Type.DELETE,
                    articleId, loginUserId, loginUserId);
            rabbitMessagePublisher.publishMsg(message);
            return true;
        }
        return false;
    }

    private boolean handleInsert(Article article, ArticleDraft draft, Integer loginUserId) {
        // 插入, 已经有文章标签ID了
        boolean success = articleRepository.saveArticle(article);
        draft.setArticleId(article.getId());
        draft.setState(Constants.ONE);
        final Integer articleId = article.getId();
        return Predicates.check(success)
                .then(() -> {
                    // 初始化文章数据
                    int rows = articleStatMapper.insert(new ArticleStat(articleId, loginUserId));
                    return rows == 1;
                })
                .then(() -> {
                    // 更新文章草稿
                    ArticleDraft newDraft = new ArticleDraft(draft.getId(), articleId, Constants.ONE, draft.getVersion());
                    return articleDraftRepository.updateDraft(newDraft);
                }).get();
    }

    private boolean handleUpdate(Article article) {
        // 直接更新即可, 不要在更新标签啦
        return articleRepository.updateArticle(article);
    }

    private void checkDraft(ArticleDraft draft) {
        validator.validate(draft);
        // 校验分类
        // assert cateId != null
        Map<Integer, Category> categroyMap = categoryService.getCategoryMap();
        if (categroyMap.containsKey(draft.getCategoryId())) {
            throw new HamiServiceException("分类不存在");
        }
        // 校验标签 若有个tagId不存在抛出错误
        List<Integer> tagIds = draft.getTagIds();
        Map<Integer, Tag> tagsMap = tagService.getTagMap();
        for (Integer tagId : tagIds) {
            if (!tagsMap.containsKey(tagId)) {
                throw new HamiServiceException("标签不存在");
            }
        }
    }

    private List<ArticleDraftVo> buildDrafts(List<ArticleDraft> drafts) {
        return ListMapperHandler.listTo(drafts, draft -> {
            List<Integer> tagsIds = draft.getTagIds();
            List<Tag> tags = tagService.getTagsByIds(tagsIds);
            return ArticleConverter.INSTANCE.toDraftDTO(draft, tags);
        }, false);
    }
}
