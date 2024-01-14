package top.wang3.hami.core.service.rank.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.dto.builder.ArticleOptionsBuilder;
import top.wang3.hami.common.dto.builder.UserOptionsBuilder;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.common.vo.article.ArticleVo;
import top.wang3.hami.common.vo.article.HotArticle;
import top.wang3.hami.common.vo.user.HotAuthor;
import top.wang3.hami.common.vo.user.UserVo;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.rank.RankListService;
import top.wang3.hami.core.service.user.UserService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankListServiceImpl implements RankListService {

    private final ArticleService articleService;
    private final UserService userService;

    @Override
    public List<HotArticle> listHotArticle(Integer cateId) {
        // 文章榜
        String key = cateId == null ? RedisConstants.TOTAL_HOT_ARTICLE : RedisConstants.CATE_HOT_ARTICLE + cateId;
        Collection<ZSetOperations.TypedTuple<Integer>> typedTuples = this.getRankList(key);
        ArrayList<Integer> ids = new ArrayList<>(typedTuples.size());
        List<HotArticle> articles = ListMapperHandler.listTo(typedTuples, item -> {
            HotArticle hotArticle = new HotArticle();
            hotArticle.setArticleId(item.getValue());
            hotArticle.setHotRank(item.getScore());
            ids.add(item.getValue());
            return hotArticle;
        }, false);
        List<ArticleVo> vos = articleService.listArticleVoById(ids, ArticleOptionsBuilder.justInfo());
        ListMapperHandler.doAssemble(
                articles,
                HotArticle::getArticleId,
                vos,
                ArticleVo::getId,
                HotArticle::setArticle
        );
        return articles;
    }

    @Override
    public List<HotAuthor> listHotAuthor() {
        // 作者榜
        String key = RedisConstants.AUTHOR_RANKING;
        Collection<ZSetOperations.TypedTuple<Integer>> typedTuples = this.getRankList(key);
        ArrayList<Integer> ids = new ArrayList<>(typedTuples.size());
        List<HotAuthor> authors = ListMapperHandler.listTo(typedTuples, item -> {
            HotAuthor author = new HotAuthor();
            author.setUserId(item.getValue());
            author.setHotIndex(item.getScore());
            ids.add(item.getValue());
            return author;
        }, false);
        List<UserVo> vos = userService.listAuthorById(ids, new UserOptionsBuilder().noStat());
        ListMapperHandler.doAssemble(
                authors,
                HotAuthor::getUserId,
                vos,
                UserVo::getUserId,
                HotAuthor::setUser
        );
        return authors;
    }

    private Collection<ZSetOperations.TypedTuple<Integer>> getRankList(String key) {
        return RedisClient.zRevRangeWithScore(key, 0, -1);
    }
}
