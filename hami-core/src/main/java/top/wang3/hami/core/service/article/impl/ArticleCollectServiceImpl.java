package top.wang3.hami.core.service.article.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.ArticleCollect;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.core.mapper.ArticleCollectMapper;
import top.wang3.hami.core.service.article.ArticleCollectService;

import java.util.List;

@Service
public class ArticleCollectServiceImpl extends ServiceImpl<ArticleCollectMapper, ArticleCollect>
        implements ArticleCollectService {

    @Override
    public Long getUserCollects(Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("user_id")
                .eq("user_id", userId)
                .eq("`state`", Constants.ONE)
                .count();
    }

    @Override
    public boolean collectArticle(int userId, int articleId) {
        ArticleCollect articleCollect = ChainWrappers.queryChain(getBaseMapper())
                .eq("user_id", userId)
                .eq("article_id", articleId)
                .one();
        if (articleCollect == null) {
            ArticleCollect collect = new ArticleCollect();
            collect.setUserId(userId);
            collect.setArticleId(articleId);
            collect.setState(Constants.ONE);
            return super.save(collect);
        } else if (Constants.ZERO.equals(articleCollect.getState())) {
            return ChainWrappers.updateChain(getBaseMapper())
                    .set("`state`", Constants.ONE)
                    .eq("user_id", userId)
                    .eq("article_id", articleId)
                    .eq("`state`", Constants.ZERO)
                    .update();
        } else if (Constants.ONE.equals(articleCollect.getState())) {
            throw new ServiceException("重复收藏");
        }
        return false;
    }

    @Override
    public boolean cancelCollectArticle(int userId, int articleId) {
        return ChainWrappers.updateChain(getBaseMapper())
                .set("`state`", Constants.ZERO)
                .eq("user_id", userId)
                .eq("article_id", articleId)
                .eq("`state`", Constants.ONE)
                .update();
    }

    @Override
    public List<ArticleCollect> getUserCollectArticles(Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("id", "article_id", "user_id", "ctime", "mtime")
                .eq("user_id", userId)
                .eq("`state`", Constants.ONE) //状态为1
                .orderByDesc("mtime") //更新时间
                .last("limit 2000")
                .list();
    }

    @Override
    public List<ArticleCollect> getUserCollectArticles(Page<ArticleCollect> page, Integer userId) {
        throw new UnsupportedOperationException("暂不支持");
    }
}
