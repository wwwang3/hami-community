package top.wang3.hami.core.service.stat.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.converter.StatConverter;
import top.wang3.hami.common.dto.stat.ArticleStatDTO;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.core.service.stat.ArticleStatService;
import top.wang3.hami.core.service.stat.repository.ArticleStatRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleStatServiceImpl implements ArticleStatService {

    private final ArticleStatRepository articleStatRepository;

    @Override
    public ArticleStatDTO getArticleStatId(int articleId) {
        ArticleStat stat = articleStatRepository.selectArticleStatById(articleId);
        return StatConverter.INSTANCE.toArticleStatDTO(stat);
    }

    @Override
    public List<ArticleStatDTO> listArticleStatById(List<Integer> articleIds) {
        List<ArticleStat> stats = articleStatRepository.selectArticleStatList(articleIds);
        return StatConverter.INSTANCE.toArticleStatDTOS(stats);
    }

}
