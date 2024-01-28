package top.wang3.hami.core.service.article;

import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.dto.article.ArticleDraftParam;
import top.wang3.hami.common.model.ArticleDraft;

public interface ArticleDraftService {

    /**
     * 分页获取文章草稿
     *
     * @param param 分页参数
     * @param state 文章状态
     * @return PageData<ArticleDraft>
     */
    PageData<ArticleDraft> listDraftByPage(PageParam param, byte state);

    /**
     * 获取文章草稿
     *
     * @param draftId 文章草稿ID
     * @return ArticleDraft
     */
    ArticleDraft getArticleDraftById(long draftId);

    ArticleDraft createDraft(ArticleDraftParam param);

    ArticleDraft updateDraft(ArticleDraftParam param);

    /**
     * 发表文章
     * @param draftId 参数
     * @return ArticleDraft
     */
    ArticleDraft publishArticle(Long draftId);

    boolean deleteDraft(long draftId);

    boolean deleteArticle(int articleId);

}
