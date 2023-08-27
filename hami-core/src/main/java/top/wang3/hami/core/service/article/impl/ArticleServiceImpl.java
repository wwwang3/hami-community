package top.wang3.hami.core.service.article.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.core.mapper.ArticleMapper;
import top.wang3.hami.core.service.article.ArticleService;


@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {
}
