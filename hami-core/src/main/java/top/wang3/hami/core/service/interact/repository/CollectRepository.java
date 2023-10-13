package top.wang3.hami.core.service.interact.repository;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.model.ArticleCollect;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface CollectRepository extends IService<ArticleCollect> {

    @Transactional(rollbackFor = Exception.class)
    boolean doCollect(Integer userId, Integer itemId);

    @Transactional(rollbackFor = Exception.class)
    boolean cancelCollect(Integer userId, Integer itemId);

    @Transactional(rollbackFor = Exception.class)
    boolean hasCollected(Integer userId, Integer itemId);

    @Transactional(rollbackFor = Exception.class)
    int deleteCollectItem(Integer articleId);

    Map<Integer, Boolean> hasCollected(Integer userId, List<Integer> itemIds);

    Long getUserCollectCount(Integer userId);

    List<ArticleCollect> listUserCollects(Integer userId);

    List<ArticleCollect> listUserCollects(Integer userId, int max);

    Collection<Integer> listUserCollects(Page<ArticleCollect> page, Integer userId);
}
