package top.wang3.hami.core.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.model.ArticleTag;

import java.util.List;


public interface ArticleTagRepository extends IService<ArticleTag> {

    List<ArticleTag> getArticleTagsById(Integer articleId);

    List<Integer> getArticleTagIdsById(Integer articleId);

    boolean saveArticleTags(Integer articleId, List<Integer> tagIds);

    /**
     *
     * @param ids 主键ID
     * @return
     */
    boolean deleteArticleTags(List<Integer> ids);
}
