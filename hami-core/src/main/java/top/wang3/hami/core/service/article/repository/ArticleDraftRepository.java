package top.wang3.hami.core.service.article.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.model.ArticleDraft;

import java.util.List;

public interface ArticleDraftRepository extends IService<ArticleDraft> {

    ArticleDraft getDraftById(Long draftId, Integer userId);

    List<ArticleDraft> getDraftsByPage(Page<ArticleDraft> page, Integer userId, byte state);

    boolean saveDraft(ArticleDraft draft);

    boolean updateDraft(ArticleDraft draft);

    boolean deleteDraftById(Long draftId, Integer userId);

    boolean deleteDraftByArticleId(Integer articleId, Integer userId);
}
