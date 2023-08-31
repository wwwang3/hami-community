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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.converter.ArticleConverter;
import top.wang3.hami.common.dto.ArticleDraftDTO;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.TagDTO;
import top.wang3.hami.common.dto.notify.ArticlePublishMsg;
import top.wang3.hami.common.dto.request.ArticleDraftParam;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.model.ArticleDraft;
import top.wang3.hami.common.model.ArticleStat;
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

    @Resource
    RabbitTemplate rabbitTemplate;

    @Resource
    Validator validator;


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
        ArticleDraft draft = super.getById(draftId);
        List<Integer> tagsIds = draft.getTagIds();
        List<TagDTO> tags = tagService.getTagByIds(tagsIds);
        return ArticleConverter.INSTANCE.toDraftDTO(draft, tags);
    }

    /**
     * 创建文章草稿
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
        UpdateWrapper<ArticleDraft> wrapper = Wrappers.update(draft)
                .set("version", oldDraft.getVersion() + 1)
                .eq("id", draft.getId())
                .eq("version", oldDraft.getVersion());
        return super.update(draft, wrapper) ? draft : null;
    }

    /**
     * todo 文章审核
     * @param draftId 草稿ID
     * @return ArticleDraft
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleDraft publishArticle(Long draftId) {
        //检查参数, 文章ID, 其他都不能为空
        //发表文章
        int loginUserId = LoginUserContext.getLoginUserId();
        //null-safe
        ArticleDraft oldDraft = getOldDraft(draftId);
        //校验draft
        checkDraft(oldDraft);
        //文章
        Article article = ArticleConverter.INSTANCE.toArticle(oldDraft);
        //保存文章
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
            super.updateById(oldDraft);
        } else {
            //更新
            success1 = articleService.updateById(article);
            articleTagService.updateTags(article.getId(), oldDraft.getTagIds());
        }
        //发布文章发表消息
        rabbitTemplate.convertAndSend(Constants.NOTIFY_EXCHANGE, Constants.NOTIFY_ROUTING,
                new ArticlePublishMsg(article.getId(), article.getUserId(), article.getTitle()));
        return success1 ? oldDraft : null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteDraft(long draftId) {
        //刪除草稿
        int userId = LoginUserContext.getLoginUserId();
        QueryWrapper<ArticleDraft> wrapper = Wrappers.query(getEntityClass())
                .eq("id", draftId)
                .eq("user_id", userId)
                .eq("`state`", Constants.ZERO);//0为草稿
        return super.remove(wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteArticle(int articleId) {
        //删除文章
        int userId = LoginUserContext.getLoginUserId();
        QueryWrapper<Article> wrapper = Wrappers.query(articleService.getEntityClass())
                .eq("user_id", userId)
                .eq("id", articleId);
        boolean deleted = articleService.remove(wrapper);
        //删除草稿
        if (deleted) {
            QueryWrapper<ArticleDraft> draftQueryWrapper = Wrappers.query(getEntityClass())
                    .eq("user_id", userId)
                    .eq("article_id", articleId);
            return super.remove(draftQueryWrapper);
        }
        return false;
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

    private List<ArticleDraftDTO> buildDrafts(List<ArticleDraft> drafts) {
        return drafts.stream()
                .map(draft -> {
                    List<Integer> tagsIds = draft.getTagIds();
                    List<TagDTO> tags = tagService.getTagByIds(tagsIds);
                    return ArticleConverter.INSTANCE.toDraftDTO(draft, tags);
                }).toList();
    }
}
