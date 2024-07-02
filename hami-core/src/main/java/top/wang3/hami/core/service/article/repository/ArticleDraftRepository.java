package top.wang3.hami.core.service.article.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.constant.ContentState;
import top.wang3.hami.common.model.ArticleDraft;

import java.util.List;

public interface ArticleDraftRepository extends IService<ArticleDraft> {

    ArticleDraft getDraftById(Long draftId, Integer userId);

    List<ArticleDraft> getDraftByPage(Page<ArticleDraft> page, Integer userId, byte state);

    List<ArticleDraft> getDraftByPage(Page<ArticleDraft> page);

    List<ArticleDraft> getDraftByPage(Page<ArticleDraft> page, byte state);

    List<ArticleDraft> getDraftByPage(Page<ArticleDraft> page, List<Byte> states);

    boolean createDraft(ArticleDraft draft);

    boolean updateDraft(ArticleDraft draft);

    // state == 0
    boolean deleteOriginDraft(long draftId, int userId);

    // state == 1 or 2 or 3
    boolean deleteDraft(long draftId, int userId);

    boolean updateDraftState(Long draftId, ContentState contentState, Long oldVersion);
}
