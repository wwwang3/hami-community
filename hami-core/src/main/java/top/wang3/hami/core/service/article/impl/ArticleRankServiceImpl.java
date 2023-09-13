package top.wang3.hami.core.service.article.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.ArticleDTO;
import top.wang3.hami.common.dto.HotArticleDTO;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.core.service.article.ArticleRankService;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.article.CategoryService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@SuppressWarnings("unchecked")
@RequiredArgsConstructor
@Slf4j
public class ArticleRankServiceImpl implements ArticleRankService {

    private final CategoryService categoryService;

    private final ArticleService articleService;

    @Override
    public List<HotArticleDTO> getHotArticles(Integer categoryId) {
        if (categoryService.getCategoryDTOById(categoryId) == null) {
            throw new ServiceException("分类不存在");
        }
        List<HotArticleDTO> counters = (categoryId == null) ? getOverallHotArticles() : getHotArticlesByCate(categoryId);
        if (counters.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> articleIds = ListMapperHandler.listTo(counters, HotArticleDTO::getArticleId);
        ArticleService.OptionsBuilder builder = new ArticleService.OptionsBuilder()
                .noTags();
        List<ArticleDTO> articles = articleService.getArticleByIds(articleIds, builder);
        ListMapperHandler.doAssemble(counters, HotArticleDTO::getArticleId,
                articles, ArticleDTO::getId, HotArticleDTO::setArticleDTO);
        return counters;
    }

    private List<HotArticleDTO> getOverallHotArticles() {
        //总榜
        String redisKey = Constants.OVERALL_HOT_ARTICLES;
        return scanHotArticles(redisKey);
    }


    private List<HotArticleDTO> getHotArticlesByCate(Integer cateId) {
        String redisKey = Constants.HOT_ARTICLE + cateId;
        return scanHotArticles(redisKey);
    }

    private List<HotArticleDTO> scanHotArticles(String redisKey) {
        try (Cursor<ZSetOperations.TypedTuple<Integer>> cursor = RedisClient.getTemplate()
                .opsForZSet()
                .scan(redisKey, ScanOptions.NONE)) {
            ArrayList<HotArticleDTO> items = new ArrayList<>();
            while (cursor.hasNext()) {
                ZSetOperations.TypedTuple<Integer> item = cursor.next();
                items.add(convertToHotCounter(item));
            }
            return items;
        } catch (Exception e) {
            log.error("error to scan items: error_class: {}, error_msg: {}",
                    e.getClass().getSimpleName(), e.getMessage());
            return Collections.emptyList();
        }
    }

    private HotArticleDTO convertToHotCounter(ZSetOperations.TypedTuple<Integer> item) {
        HotArticleDTO dto = new HotArticleDTO();
        dto.setArticleId(item.getValue());
        Long score = item.getScore() == null ? 0L : item.getScore().longValue();
        dto.setHotRank(score);
        return dto;
    }

}
