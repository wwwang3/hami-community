package top.wang3.hami.core.service.interact.repository;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.model.ArticleCollect;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public interface CollectRepository extends IService<ArticleCollect> {

    boolean doCollect(Integer userId, Integer itemId);

    boolean cancelCollect(Integer userId, Integer itemId);

    boolean collectArticle(Integer userId, Integer articleId, byte state);

    boolean hasCollected(Integer userId, Integer itemId);

    Map<Integer, Boolean> hasCollected(Integer userId, List<Integer> itemIds);

    @Transactional(rollbackFor = Exception.class)
    int deleteCollectItem(Integer articleId);

    Integer getUserCollectCount(Integer userId);

    List<ArticleCollect> listUserCollects(Integer userId);

    List<ArticleCollect> listUserCollects(Integer userId, int max);

    Collection<Integer> listUserCollects(Page<ArticleCollect> page, Integer userId);
}
