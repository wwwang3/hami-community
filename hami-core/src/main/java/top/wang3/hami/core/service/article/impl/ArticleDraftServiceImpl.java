package top.wang3.hami.core.service.article.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.converter.ArticleConverter;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.notify.ArticlePublishMsg;
import top.wang3.hami.common.dto.request.ArticleDraftParam;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.model.ArticleDraft;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.core.mapper.ArticleDraftMapper;
import top.wang3.hami.core.mapper.ArticleStatMapper;
import top.wang3.hami.core.service.article.ArticleDraftService;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.article.ArticleTagService;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.List;

@Service
@Slf4j
public class ArticleDraftServiceImpl extends ServiceImpl<ArticleDraftMapper, ArticleDraft>
        implements ArticleDraftService {

    private final ArticleService articleService;
    private final ArticleTagService articleTagService;
    private final ArticleStatMapper articleStatMapper;

    @Resource
    RabbitTemplate rabbitTemplate;

    public ArticleDraftServiceImpl(ArticleService articleService, ArticleTagService articleTagService,
                                   ArticleStatMapper articleStatMapper) {
        this.articleService = articleService;
        this.articleTagService = articleTagService;
        this.articleStatMapper = articleStatMapper;
    }

    @Override
    public PageData<ArticleDraft> getArticleDrafts(PageParam param, byte state) {
        int loginUserId = LoginUserContext.getLoginUserId();
        Page<ArticleDraft> page = param.toPage();
        List<ArticleDraft> drafts = ChainWrappers.queryChain(getBaseMapper())
                .eq("user_id", loginUserId)
                .eq("`state`", state)
                .list(page);
        return PageData.<ArticleDraft>builder()
                .pageNum(page.getCurrent())
                .pageSize(page.getSize())
                .total(page.getTotal())
                .data(drafts)
                .build();
    }

    @Override
    public ArticleDraft getArticleDraftById(long draftId) {
        return super.getById(draftId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ArticleDraft saveOrUpdateArticleDraft(ArticleDraftParam param) {
        ArticleDraft draft = ArticleConverter.INSTANCE.toArticleDraft(param);
        draft.setArticleId(null); //内部查询一次articleId
        boolean saved = saveOrUpdateDraft(draft);
        return saved ? draft : null;
    }


    /**
     * todo 文章审核
     * @param param 参数
     * @return ArticleDraft
     */
    @Override
    @Transactional
    public ArticleDraft publishArticle(ArticleDraftParam param) {
        //检查参数, 文章ID, 其他都不能为空
        //发表文章
        int loginUserId = LoginUserContext.getLoginUserId();
        ArticleDraft draft = saveOrUpdateArticleDraft(param);
        Article article = ArticleConverter.INSTANCE.toArticle(draft);
        //设置作者
        article.setUserId(loginUserId);
        //保存文章
        boolean success1 = articleService.save(article);
        //保存文章标签
        articleTagService.saveTags(article.getId(), draft.getArticleTags());
        //文章数据表
        articleStatMapper.insert(new ArticleStat(article.getId(), loginUserId));
        //更新或者保存草稿
        draft.setArticleId(article.getId());
        draft.setState(Constants.ONE);
        boolean success2 = saveOrUpdateDraft(draft);
        //发布文章发表消息
        rabbitTemplate.convertAndSend(Constants.NOTIFY_EXCHANGE, Constants.NOTIFY_ROUTING,
                new ArticlePublishMsg(article.getId(), article.getUserId(), article.getTitle()));
        return success2 && success1 ? draft : null;
    }


    /**
     * 内部调用
     * @param draft draft
     * @return boolean
     */
    private boolean saveOrUpdateDraft(ArticleDraft draft) {
        if (draft == null) return false;
        Long id = draft.getId();
        if (id == null) { //插入记录
            int userId = LoginUserContext.getLoginUserId();
            draft.setUserId(userId);
            return save(draft);
        }
        //存在ID时更新记录
        ArticleDraft oldDraft = super.getById(id);
        if (oldDraft == null) {
            log.debug("draft-id 参数异常: {}", draft.getId());
            throw new ServiceException("draftId参数异常");
        }
        if (oldDraft.getArticleId() != null && draft.getArticleId() != null) {
            //发布文章时可能发生
            if (!oldDraft.getArticleId().equals(draft.getArticleId())) {
                //不一致
                log.debug("文章Id参数异常, old-{} new-{}", oldDraft.getArticleId(), draft.getArticleId());
                throw new ServiceException("文章ID参数异常");
            }
        }
        UpdateWrapper<ArticleDraft> wrapper = Wrappers.update(draft)
                .set("version", oldDraft.getVersion() + 1)
                .eq("id", draft.getId())
                .eq("version", oldDraft.getVersion());
        boolean success = super.update(draft, wrapper);
        if (success && oldDraft.getArticleId() != null) {
            draft.setArticleId(oldDraft.getArticleId());
            updateArticle(draft);
        }
        return success;
    }

    private void updateArticle(ArticleDraft draft) {
        //更新文章
        Article article = ArticleConverter.INSTANCE.toArticle(draft);
        Assert.notNull(article.getId(), "articleId can not be null");
        articleService.updateById(article);
        //更新标签
        List<Integer> tagIds = draft.getArticleTags();
        articleTagService.updateTags(article.getId(), tagIds);
    }


}
