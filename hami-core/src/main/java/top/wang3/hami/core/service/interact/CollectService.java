package top.wang3.hami.core.service.interact;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import top.wang3.hami.common.model.ArticleCollect;

import java.util.List;
import java.util.Map;

public interface CollectService {

    boolean doCollect(Integer itemId);

    boolean cancelCollect(Integer itemId);

    boolean hasCollected(Integer userId, Integer itemId);

    Map<Integer, Boolean> hasCollected(Integer userId, List<Integer> itemIds);

    Long getUserCollectCount(Integer userId);

    List<Integer> listUserCollects(Page<ArticleCollect> page, Integer userId);

    List<Integer> loadUserCollects(Integer userId);
}
