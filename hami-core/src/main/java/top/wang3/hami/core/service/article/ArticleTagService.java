package top.wang3.hami.core.service.article;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.dto.ArticleTagDTO;
import top.wang3.hami.common.dto.TagDTO;
import top.wang3.hami.common.model.ArticleTag;

import java.util.List;

public interface ArticleTagService extends IService<ArticleTag> {

    void updateTags(Integer articleId, List<Integer> newTags);

    void saveTags(Integer id, List<Integer> tagIds);

    List<ArticleTagDTO> listArticleTagByArticleIds(List<Integer> articleIds);

    List<TagDTO> getArticleTagByArticleId(int articleId);
}
