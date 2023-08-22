package top.wang3.hami.core.service.article;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.model.ArticleCollect;

public interface ArticleCollectService extends IService<ArticleCollect> {

    Long getUserCollects(Integer userId);
}
