package top.wang3.hami.core.service.stat;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import top.wang3.hami.common.dto.stat.ArticleStatDTO;

import java.util.List;

public interface ArticleStatService {

    ArticleStatDTO getArticleStatId(int articleId);

    List<ArticleStatDTO> listArticleStatById(List<Integer> articleIds);

    @CanIgnoreReturnValue
    boolean increaseComments(int articleId, int count);

    @CanIgnoreReturnValue
    boolean decreaseComments(int articleId, int count);

}
