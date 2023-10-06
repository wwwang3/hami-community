package top.wang3.hami.core.service.article.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.model.ArticleDraft;

import java.util.List;

public interface ArticleDraftRepository extends IService<ArticleDraft> {

    ArticleDraft getDraftById(Long draftId, Integer userId);

    List<ArticleDraft> getDraftsByPage(Page<ArticleDraft> page, Integer userId, byte state);

    @Transactional(rollbackFor = Exception.class)
    boolean saveDraft(ArticleDraft draft);
    @Transactional(rollbackFor = Exception.class)
    boolean updateDraft(ArticleDraft draft);

    @Transactional(rollbackFor = Exception.class)
    boolean deleteDraftById(Long draftId, Integer userId);

    @Transactional(rollbackFor = Exception.class)
    boolean deleteDraftByArticleId(Integer articleId, Integer userId);
}
