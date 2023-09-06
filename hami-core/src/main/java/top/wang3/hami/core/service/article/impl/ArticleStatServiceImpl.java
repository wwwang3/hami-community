package top.wang3.hami.core.service.article.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.converter.ArticleConverter;
import top.wang3.hami.common.dto.ArticleStatDTO;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.core.mapper.ArticleStatMapper;
import top.wang3.hami.core.service.article.ArticleStatService;

import java.util.List;

@Service
public class ArticleStatServiceImpl extends ServiceImpl<ArticleStatMapper, ArticleStat>
        implements ArticleStatService {

    @Override
    public ArticleStatDTO getArticleStatByArticleId(int articleId) {
        ArticleStat stat = ChainWrappers.queryChain(getBaseMapper())
                .eq("article_id", articleId)
                .one();
        return ArticleConverter.INSTANCE.toArticleStatDTO(stat);
    }

    @Override
    public List<ArticleStatDTO> getArticleStatByArticleIds(List<Integer> articleIds) {
        List<ArticleStat> stats = ChainWrappers.queryChain(getBaseMapper())
                .in("article_id", articleIds)
                .list();
        return ArticleConverter.INSTANCE.toArticleStatDTOList(stats);
    }

    @Override
    public List<ArticleStat> scanArticleStats(int lastArticle, int batchSize) {
        int limit = Math.min(100, batchSize);
        return getBaseMapper().scanBatchStats(lastArticle, limit);
    }


    public ArticleStatDTO getArticleStat(int articleId) {
        //依次查询阅读量/收藏数量/评论数/收藏数
        return null;
    }

    @Override
    public boolean increaseViews(int articleId, int count) {
        return ChainWrappers.updateChain(getBaseMapper())
                .setSql("views = views + {0}", count)
                .eq("article_id", articleId)
                .update();
    }

    @Override
    public boolean increaseCollects(int articleId, int count) {
        return ChainWrappers.updateChain(getBaseMapper())
                .setSql("collects = collects + {0}", count)
                .eq("article_id", articleId)
                .update();
    }

    @Override
    public boolean increaseComments(int articleId, int count) {
        return ChainWrappers.updateChain(getBaseMapper())
                .setSql("comments = comment + {0}", count)
                .eq("article_id", articleId)
                .update();
    }

    @Override
    public boolean increaseLikes(int articleId, int count) {
        return ChainWrappers.updateChain(getBaseMapper())
                .setSql("likes = likes + {0}", count)
                .eq("article_id", articleId)
                .update();
    }

    @Override
    public boolean decreaseCollects(int articleId, int count) {
        return ChainWrappers.updateChain(getBaseMapper())
                .setSql("collects = collect - {0}", count)
                .eq("article_id", articleId)
                .update();
    }

    @Override
    public boolean decreaseLikes(int articleId, int count) {
        return ChainWrappers.updateChain(getBaseMapper())
                .setSql("likes = likes - {0}", count)
                .eq("article_id", articleId)
                .update();
    }

    @Override
    public boolean decreaseComments(int articleId, int count) {
        return ChainWrappers.updateChain(getBaseMapper())
                .setSql("comments = comments + {0}", count)
                .eq("article_id", articleId)
                .update();
    }
}
