package top.wang3.hami.core.repository.impl;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.ArticleDraft;
import top.wang3.hami.core.mapper.ArticleDraftMapper;
import top.wang3.hami.core.repository.ArticleDraftRepository;

import java.util.List;

@Repository
public class ArticleDraftRepositoryImpl extends ServiceImpl<ArticleDraftMapper, ArticleDraft>
        implements ArticleDraftRepository {

    @Override
    public ArticleDraft getDraftById(Long draftId, Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .eq("id", draftId)
                .eq("user_id", userId) //当前登录用户的草稿
                .one();
    }

    @Override
    public List<ArticleDraft> getDraftsByPage(Page<ArticleDraft> page, Integer userId, byte state) {
        return ChainWrappers.queryChain(getBaseMapper())
                .eq("user_id", userId)
                .eq("`state`", state)
                .list(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveDraft(ArticleDraft draft) {
        Assert.notNull(draft, "draft cannot be null");
        return super.save(draft);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateDraft(ArticleDraft draft) {
        Assert.notNull(draft, "draft cannot be null");
        UpdateWrapper<ArticleDraft> wrapper = Wrappers.<ArticleDraft>update()
                .setSql("version = version + 1")
                .eq("id", draft.getId())
                .eq("version", draft.getVersion());
        return super.update(draft, wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteDraftById(Long draftId, Integer userId) {
        return ChainWrappers.updateChain(getBaseMapper())
                .eq("id", draftId)
                .eq("user_id", userId)
                .eq("`state`", Constants.ZERO) //0为草稿
                .remove();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteDraftByArticleId(Integer articleId, Integer userId) {
       return  ChainWrappers.updateChain(getBaseMapper())
                .eq("user_id", userId)
                .eq("article_id", articleId)
                .remove();
    }
}