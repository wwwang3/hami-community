package top.wang3.hami.core.service.common;

import top.wang3.hami.common.dto.ArticleStatDTO;
import top.wang3.hami.common.dto.UserStat;

/**
 * 计数业务接口
 * 文章数据
 * 用户数据
 * 评论数据
 */
public interface CountService {

    ArticleStatDTO getArticleStatById(int articleId);

    UserStat getUserStatById(Integer userId);

    void increaseViews(Integer id);
}
