package top.wang3.hami.core.service.common;

import top.wang3.hami.common.dto.ArticleStatDTO;
import top.wang3.hami.common.dto.UserStat;
import top.wang3.hami.common.model.ArticleStat;

import java.util.Collection;

/**
 * 计数业务接口
 * 文章数据
 * 用户数据
 * 评论数据
 */
public interface CountService {

    ArticleStatDTO getArticleStatById(int articleId);

    UserStat getUserStatById(Integer userId);

    void increaseViews(Collection<ArticleStat> stats);

}
