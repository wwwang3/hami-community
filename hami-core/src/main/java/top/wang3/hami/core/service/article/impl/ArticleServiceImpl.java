package top.wang3.hami.core.service.article.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.dto.ArticleDTO;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.request.ArticlePageParam;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.core.mapper.ArticleMapper;
import top.wang3.hami.core.service.article.ArticleService;


@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article>
        implements ArticleService {


    @Override
    public PageData<ArticleDTO> listRecommendsArticles(ArticlePageParam param) {
        //获取推荐文章列表
        Integer cateId = param.getCateId();
        Page<Article> page = param.toPage();
        super.getBaseMapper().selectArticlesByCategoryId(page, cateId);
        //查询文章信息
        //查询分类信息
        //查询标签信息
        //查询作者信息
        //查询文章数据
        //用户行为
        return null;
    }
}
