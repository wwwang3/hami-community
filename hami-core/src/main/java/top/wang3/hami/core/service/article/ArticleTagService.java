package top.wang3.hami.core.service.article;

import java.util.List;

public interface ArticleTagService {

    List<Integer> getArticleTagIds(Integer articleId);

    boolean updateTags(Integer articleId, List<Integer> newTags);

    void saveTags(Integer id, List<Integer> tagIds);
}
