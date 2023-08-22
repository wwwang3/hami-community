package top.wang3.hami.core.service.article.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.ArticleCollect;
import top.wang3.hami.core.mapper.ArticleCollectMapper;
import top.wang3.hami.core.service.article.ArticleCollectService;

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
}
