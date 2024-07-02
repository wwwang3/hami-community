package top.wang3.hami.core.service.article.repository;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import top.wang3.hami.common.constant.ContentState;
import top.wang3.hami.common.model.ArticleDraft;
import top.wang3.hami.core.mapper.ArticleDraftMapper;

import java.util.List;

@Repository
public class ArticleDraftRepositoryImpl extends ServiceImpl<ArticleDraftMapper, ArticleDraft>
        implements ArticleDraftRepository {

    public static final String[] FIELDS = {"id", "user_id", "article_id", "title", "picture",
            "summary", "tag_ids", "category_id", "`state`", "ctime", "mtime"
    };

    @Override
    public ArticleDraft getDraftById(Long draftId, Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .eq("id", draftId)
                .eq("user_id", userId)
                .one();
    }

    @Override
    public List<ArticleDraft> getDraftByPage(Page<ArticleDraft> page, Integer userId, byte state) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select(FIELDS) // no content
                .eq("user_id", userId)
                .eq("`state`", state)
                .orderByDesc("mtime")
                .list(page);
    }

    @Override
    public List<ArticleDraft> getDraftByPage(Page<ArticleDraft> page) {
        return ChainWrappers.queryChain(getBaseMapper())
            .orderByDesc("mtime")
            .list(page);
    }

    @Override
    public List<ArticleDraft> getDraftByPage(Page<ArticleDraft> page, byte state) {
        return ChainWrappers.queryChain(getBaseMapper())
            .eq("`state`", state)
            .orderByDesc("mtime")
            .list(page);
    }

    @Override
    public List<ArticleDraft> getDraftByPage(Page<ArticleDraft> page, List<Byte> states) {
        return ChainWrappers.queryChain(getBaseMapper())
            .in("`state`", states)
            .orderByDesc("mtime")
            .list(page);
    }

    @Override
    public boolean createDraft(ArticleDraft draft) {
        Assert.notNull(draft, "draft cannot be null");
        return super.save(draft);
    }

    @Override
    public boolean updateDraft(ArticleDraft draft) {
        Assert.notNull(draft, "draft cannot be null");
        UpdateWrapper<ArticleDraft> wrapper = Wrappers.<ArticleDraft>update()
                .setSql("version = version + 1")
                .eq("id", draft.getId())
                .eq("version", draft.getVersion());
        return super.update(draft, wrapper);
    }

    @Override
    public boolean deleteOriginDraft(long draftId, int userId) {
        return ChainWrappers.updateChain(getBaseMapper())
            .eq("id", draftId)
            .eq("user_id", userId)
            .eq("`state`", ContentState.ORIGIN_DRAFT.value) //0为草稿
            .remove();
    }

    @Override
    public boolean deleteDraft(long draftId, int userId) {
        return ChainWrappers.updateChain(getBaseMapper())
            .eq("id", draftId)
            .eq("user_id", userId)
            .remove();
    }

    @Override
    public boolean updateDraftState(Long draftId, ContentState contentState, Long oldVersion) {
        return ChainWrappers.updateChain(getBaseMapper())
            .set("`state`", contentState.value)
            .setSql( "version = version + 1")
            .eq("id", draftId)
            .eq("version", oldVersion)
            .update();
    }
}
