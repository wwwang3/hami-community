package top.wang3.hami.core.service.common;

import top.wang3.hami.common.dto.ArticleStatDTO;
import top.wang3.hami.common.dto.UserStat;
import top.wang3.hami.common.model.ArticleStat;

import java.util.Collection;

/**
 * 计数业务接口
 * 文章数据
 * 用户数据
 * 评论数据 //要全量同步数据库数据到Redis
 * // 重构：使用CacheAside模式更新数据库的同时，删除缓存
 */
public interface CountService {

    ArticleStatDTO getArticleStatById(int articleId);

    UserStat getUserStatById(Integer userId);

    void increaseViews(Collection<ArticleStat> stats);

}
