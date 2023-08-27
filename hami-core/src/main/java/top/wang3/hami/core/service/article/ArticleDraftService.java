package top.wang3.hami.core.service.article;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.request.ArticleDraftParam;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.model.ArticleDraft;

public interface ArticleDraftService extends IService<ArticleDraft> {

    /**
     * 获取文章草稿
     * @param param 分页参数
     * @return PageData<ArticleDraftDTO>
     */
    PageData<ArticleDraft> getArticleDrafts(PageParam param, byte state);

    /**
     * 获取文章草稿
     * @param draftId 文章草稿ID
     * @return ArticleDraft
     */
    ArticleDraft getArticleDraftById(long draftId);

    /**
     * 保存或者更新文章草稿
     * @param param 参数
     * @return ArticleDraft
     */
    ArticleDraft saveOrUpdateArticleDraft(ArticleDraftParam param);

    /**
     * 发表文章
     * @param param 参数
     * @return ArticleDraft
     */
    ArticleDraft publishArticle(ArticleDraftParam param);
}
