package top.wang3.hami.core.service.article.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.converter.StatConverter;
import top.wang3.hami.common.dto.stat.ArticleStatDTO;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.core.service.stat.ArticleStatService;
import top.wang3.hami.core.service.stat.repository.ArticleStatRepository;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleStatServiceImpl implements ArticleStatService {

    private final ArticleStatRepository articleStatRepository;

    @Override
    public ArticleStatDTO getArticleStatByArticleId(int articleId) {
        ArticleStat stat = articleStatRepository.getArticleStatById(articleId);
        return StatConverter.INSTANCE.toArticleStatDTO(stat);
    }

    @Override
    public Map<Integer, ArticleStatDTO> listArticleStat(List<Integer> articleIds) {
        return articleStatRepository.getArticleStatByIds(articleIds);
    }


    @Override
    public boolean increaseViews(int articleId, int count) {
        return articleStatRepository.increaseViews(articleId, count);
    }

    @Override
    public boolean increaseCollects(int articleId, int count) {
        return articleStatRepository.increaseCollects(articleId, count);
    }

    @Override
    public boolean increaseComments(int articleId, int count) {
        return articleStatRepository.increaseComments(articleId, count);
    }

    @Override
    public boolean increaseLikes(int articleId, int count) {
        return articleStatRepository.increaseLikes(articleId, count);
    }

    @Override
    public boolean decreaseCollects(int articleId, int count) {
        return articleStatRepository.decreaseCollects(articleId, count);
    }

    @Override
    public boolean decreaseLikes(int articleId, int count) {
        return articleStatRepository.decreaseLikes(articleId, count);
    }

    @Override
    public boolean decreaseComments(int articleId, int count) {
        return articleStatRepository.decreaseComments(articleId, count);
    }
}
