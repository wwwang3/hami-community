package top.wang3.hami.core.service.article.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.converter.ArticleConverter;
import top.wang3.hami.common.dto.article.ArticleStatDTO;
import top.wang3.hami.common.dto.user.UserStat;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.core.service.article.ArticleStatService;
import top.wang3.hami.core.service.article.repository.ArticleStatRepository;

import java.util.Collections;
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
        return ArticleConverter.INSTANCE.toArticleStatDTO(stat);
    }

    @Override
    public Map<Integer, ArticleStatDTO> listArticleStat(List<Integer> articleIds) {
        return articleStatRepository.getArticleStatByIds(articleIds);
    }

    @NonNull
    @Override
    public UserStat getUserStatByUserId(Integer userId) {
        UserStat stat = articleStatRepository.getUserStatByUserId(userId);
        if (stat == null) {
            stat = new UserStat();
            stat.setUserId(userId);
        }
        return stat;
    }

    @NonNull
    @Override
    public Map<Integer, UserStat> listUserStat(List<Integer> userIds) {
        if (CollectionUtils.isEmpty(userIds)) return Collections.emptyMap();
        Map<Integer, UserStat> statMap = articleStatRepository.getUserStatByUserIds(userIds);
        for (Integer userId : userIds) {
            statMap.computeIfAbsent(userId, UserStat::new);
        }
        return statMap;
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
