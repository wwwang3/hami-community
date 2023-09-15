package top.wang3.hami.core.service.article.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.converter.ArticleConverter;
import top.wang3.hami.common.dto.ArticleStatDTO;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.core.repository.ArticleStatRepository;
import top.wang3.hami.core.service.article.ArticleStatService;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleStatServiceImpl implements ArticleStatService {

    private final ArticleStatRepository articleStatRepository;

    @Override
    public ArticleStatDTO getArticleStatByArticleId(int articleId) {
        ArticleStat stat = articleStatRepository.getArticleStatById(articleId);
        return ArticleConverter.INSTANCE.toArticleStatDTO(stat);
    }

    @Override
    public List<ArticleStatDTO> getArticleStatByArticleIds(List<Integer> articleIds) {
        List<ArticleStat> stats = articleStatRepository.getArticleStatByIds(articleIds);
        return ArticleConverter.INSTANCE.toArticleStatDTOList(stats);
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
