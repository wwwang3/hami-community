package top.wang3.hami.core.service.interact.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.ArticleCollect;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.core.mapper.ArticleCollectMapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CollectRepositoryImpl extends ServiceImpl<ArticleCollectMapper, ArticleCollect> 
        implements CollectRepository {

    @Override
    public boolean doCollect(Integer userId, Integer itemId) {
        ArticleCollect articleCollect = ChainWrappers.queryChain(getBaseMapper())
                .eq("user_id", userId)
                .eq("article_id", itemId)
                .one();
        if (articleCollect == null) {
            ArticleCollect collect = new ArticleCollect();
            collect.setUserId(userId);
            collect.setArticleId(itemId);
            collect.setState(Constants.ONE);
            return super.save(collect);
        } else if (Constants.ZERO.equals(articleCollect.getState())) {
            return ChainWrappers.updateChain(getBaseMapper())
                    .set("`state`", Constants.ONE)
                    .eq("user_id", userId)
                    .eq("article_id", itemId)
                    .eq("`state`", Constants.ZERO)
                    .update();
        } else if (Constants.ONE.equals(articleCollect.getState())) {
            throw new ServiceException("重复收藏");
        }
        return false;
    }

    @Override
    public boolean cancelCollect(Integer userId, Integer itemId) {
        return ChainWrappers.updateChain(getBaseMapper())
                .set("`state`", Constants.ZERO)
                .eq("user_id", userId)
                .eq("article_id", itemId)
                .eq("`state`", Constants.ONE)
                .update();
    }

    @Override
    public boolean hasCollected(Integer userId, Integer itemId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .eq("user_id", userId)
                .eq("article_id", itemId)
                .eq("state", Constants.ONE)
                .exists();
    }

    @Override
    public int deleteCollectItem(Integer articleId) {
        var wrapper = Wrappers.update(new ArticleCollect())
                .eq("article_id", articleId);
        return getBaseMapper().delete(wrapper);
    }

    @Override
    public Map<Integer, Boolean> hasCollected(Integer userId, List<Integer> itemIds) {
        List<ArticleCollect> collects = ChainWrappers.queryChain(getBaseMapper())
                .eq("user_id", userId)
                .eq("state", Constants.ONE)
                .in("article_id", itemIds)
                .list();
        return ListMapperHandler.listToMap(collects, ArticleCollect::getArticleId, i -> Boolean.TRUE);
    }

    @Override
    public Long getUserCollectCount(Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .eq("user_id", userId)
                .eq("`state`", Constants.ONE)
                .count();
    }

    @Override
    public List<ArticleCollect> listUserCollects(Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("article_id", "user_id", "mtime")
                .eq("user_id", userId)
                .eq("`state`", Constants.ONE) //状态为1
                .orderByDesc("mtime") //更新时间
                .list();
    }

    @Override
    public List<ArticleCollect> listUserCollects(Integer userId, int max) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("article_id", "user_id", "mtime")
                .eq("user_id", userId)
                .eq("`state`", Constants.ONE) //状态为1
                .orderByDesc("mtime") //更新时间
                .last("limit " + max)
                .list();
    }

    @Override
    public Collection<Integer> listUserCollects(Page<ArticleCollect> page, Integer userId) {
        List<ArticleCollect> collects = ChainWrappers.queryChain(getBaseMapper())
                .select("article_id")
                .eq("user_id", userId)
                .eq("`state`", Constants.ONE) //状态为1
                .orderByDesc("mtime") //更新时间
                .list(page);
        return ListMapperHandler.listTo(collects, ArticleCollect::getArticleId);
    }
}
