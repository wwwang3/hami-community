package top.wang3.hami.core.service.admin;

import top.wang3.hami.common.dto.ArticleDraftPageParam;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.model.ArticleDraft;
import top.wang3.hami.common.vo.article.ArticleDraftVo;

public interface ArticleAdminService {

    void reviewSuccess(ArticleDraft draft);

    void reviewFailed(ArticleDraft draft, String  msg);

    PageData<ArticleDraftVo> listReviewDraft(ArticleDraftPageParam param);

    void reviewArticle(ArticleDraft draft, String msg);
}
