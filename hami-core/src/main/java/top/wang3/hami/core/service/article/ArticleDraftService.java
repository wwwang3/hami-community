package top.wang3.hami.core.service.article;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.dto.ArticleDraftDTO;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.request.ArticleDraftParam;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.model.ArticleDraft;

public interface ArticleDraftService extends IService<ArticleDraft> {

    /**
     * 获取文章草稿
     *
     * @param param 分页参数
     * @return PageData<ArticleDraftDTO>
     */
    PageData<ArticleDraftDTO> getArticleDrafts(PageParam param, byte state);

    /**
     * 获取文章草稿
     *
     * @param draftId 文章草稿ID
     * @return ArticleDraft
     */
    ArticleDraftDTO getArticleDraftById(long draftId);

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