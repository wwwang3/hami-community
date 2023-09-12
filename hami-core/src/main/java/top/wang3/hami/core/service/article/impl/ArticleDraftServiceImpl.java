package top.wang3.hami.core.service.article.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import jakarta.annotation.Resource;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.converter.ArticleConverter;
import top.wang3.hami.common.dto.ArticleDraftDTO;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.notify.ArticlePublishMsg;
import top.wang3.hami.common.dto.request.ArticleDraftParam;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.model.ArticleDraft;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.model.Tag;
import top.wang3.hami.core.component.NotifyMsgPublisher;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.core.mapper.ArticleDraftMapper;
import top.wang3.hami.core.mapper.ArticleStatMapper;
import top.wang3.hami.core.service.article.*;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleDraftServiceImpl extends ServiceImpl<ArticleDraftMapper, ArticleDraft>
        implements ArticleDraftService {

    private final ArticleService articleService;
    private final ArticleTagService articleTagService;
    private final ArticleStatMapper articleStatMapper;

    private final CategoryService categoryService;
    private final TagService tagService;
    private final NotifyMsgPublisher notifyMsgPublisher;

    @Resource
    Validator validator;

    @Resource
    TransactionTemplate transactionTemplate;


    @Override
    public PageData<ArticleDraftDTO> getArticleDrafts(PageParam param, byte state) {
        int loginUserId = LoginUserContext.getLoginUserId();
        Page<ArticleDraft> page = param.toPage();
        List<ArticleDraft> drafts = ChainWrappers.queryChain(getBaseMapper())
                .eq("user_id", loginUserId)
                .eq("`state`", state)
                .list(page);
        //获取标签 n + 1?
        List<ArticleDraftDTO> dtos = buildDrafts(drafts);
        return PageData.<ArticleDraftDTO>builder()
                .pageNum(page.getCurrent())
                .pageSize(page.getSize())
                .total(page.getTotal())
                .data(dtos)
                .build();
    }

    @Override
    public ArticleDraftDTO getArticleDraftById(long draftId) {
        ArticleDraft draft = super.getOptById(draftId)
                .orElseThrow(() -> new ServiceException("草稿不存在"));
        log.debug("draft-{}", draft);
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
    @Transactional(rollbackFor = Exception.class)
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
        boolean saved = super.save(draft);
        return saved ? draft : null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ArticleDraft updateDraft(ArticleDraftParam param) {
        //更新草稿
        ArticleDraft draft = ArticleConverter.INSTANCE.toArticleDraft(param);
        if (draft.getId() == null) {
            throw new ServiceException("参数错误");
        }
        //获取旧的 会保证是当前用户的
        ArticleDraft oldDraft = getOldDraft(draft.getId());
        return handleUpdate(oldDraft);
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
        //发表文章
        int loginUserId = LoginUserContext.getLoginUserId();
        //null-safe
        final ArticleDraft oldDraft = getOldDraft(draftId);
        //校验draft
        checkDraft(oldDraft);
        //文章
        Article article = ArticleConverter.INSTANCE.toArticle(oldDraft);
        Boolean success = transactionTemplate.execute(status -> {
            boolean success1;
            if (article.getId() == null) {
                //插入
                success1 = articleService.save(article);
                oldDraft.setArticleId(article.getId());
                oldDraft.setState(Constants.ONE);
                //插入文章标签
                articleTagService.saveTags(article.getId(), oldDraft.getTagIds());
                //初始化文章数据
                articleStatMapper.insert(new ArticleStat(article.getId(), loginUserId));
                ArticleDraft draft = new ArticleDraft(oldDraft.getId(), article.getId(), Constants.ONE, oldDraft.getVersion());
                handleUpdate(draft);
            } else {
                //更新
                success1 = articleService.updateById(article);
                articleTagService.updateTags(article.getId(), oldDraft.getTagIds());
            }
            return success1;
        });
        if (Boolean.TRUE.equals(success)) {
            //发布文章发表消息
            ArticlePublishMsg articlePublishMsg = new ArticlePublishMsg(article.getId(), article.getUserId(), article.getTitle());
            notifyMsgPublisher.publishNotify(articlePublishMsg);
            return oldDraft;
        }
        return null;

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteDraft(long draftId) {
        //刪除草稿
        int userId = LoginUserContext.getLoginUserId();
        return ChainWrappers.updateChain(getBaseMapper())
                .eq("id", draftId)
                .eq("user_id", userId)
                .eq("`state`", Constants.ZERO) //0为草稿
                .remove();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteArticle(int articleId) {
        //删除文章
        int userId = LoginUserContext.getLoginUserId();
        boolean deleted = articleService.deleteByArticleId(userId, articleId);
        //删除草稿
        if (deleted) {
            boolean success1 = deleteDraftByArticleId(userId, articleId);
            boolean success2 = articleStatMapper.deleteById(articleId) == 1;
            return success1 && success2;
        }
        return false;
    }

    private boolean deleteDraftByArticleId(Integer userId, Integer articleId) {
        QueryWrapper<ArticleDraft> draftQueryWrapper = Wrappers.query(getEntityClass())
                .eq("user_id", userId)
                .eq("article_id", articleId);
        return super.remove(draftQueryWrapper);
    }

    private void checkDraft(ArticleDraft draft) {
        validator.validate(draft);
        //校验分类
        if (categoryService.getById(draft.getCategoryId()) == null) {
            throw new ServiceException("请选择一个分类");
        }
        ///校验标签 若有个tagId不存在抛出错误
        List<Integer> tagIds = draft.getTagIds();
        Long count = ChainWrappers.queryChain(tagService.getBaseMapper())
                .in("id", tagIds)
                .count();
        if (count == null || count.intValue() != tagIds.size()) {
            throw new ServiceException("参数错误");
        }
    }

    /**
     * 获取旧的草稿
     *
     * @param draftId 草稿ID
     * @return ArticleDraft
     * @throws ServiceException 不存在时抛出异常
     */
    private ArticleDraft getOldDraft(Long draftId) {
        int userId = LoginUserContext.getLoginUserId();
        return ChainWrappers.queryChain(getBaseMapper())
                .eq("id", draftId)
                .eq("user_id", userId) //当前登录用户的草稿
                .oneOpt()
                .orElseThrow(() -> new ServiceException("草稿不存在"));
    }

    private ArticleDraft handleUpdate(ArticleDraft oldDraft) {
        UpdateWrapper<ArticleDraft> wrapper = Wrappers.<ArticleDraft>update()
                .set("version", oldDraft.getVersion() + 1)
                .eq("id", oldDraft.getId())
                .eq("version", oldDraft.getVersion());
        return super.update(oldDraft, wrapper) ? oldDraft : null;
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
