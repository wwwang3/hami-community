package top.wang3.hami.common.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import top.wang3.hami.common.dto.article.ArticleDraftParam;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.model.ArticleDraft;
import top.wang3.hami.common.vo.article.ArticleVo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ArticleConverter {

    ArticleConverter INSTANCE = Mappers.getMapper(ArticleConverter.class);

    @Mappings(value = {
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "state", ignore = true),
            @Mapping(target = "mtime", ignore = true),
            @Mapping(target = "deleted", ignore = true),
            @Mapping(target = "ctime", ignore = true),
            @Mapping(target = "articleId", ignore = true),
            @Mapping(target = "userId", ignore = true)
    })
    ArticleDraft toArticleDraft(ArticleDraftParam param);


    @Mapping(target = "id", source = "articleId")
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "mtime", ignore = true)
    @Mapping(target = "ctime", ignore = true)
    Article toArticle(ArticleDraft draft);


    default ArticleVo toArticleVo(Article articleInfo) {
        if (articleInfo == null) return null;
        ArticleVo vo = new ArticleVo();

        vo.setId(articleInfo.getId());
        vo.setUserId(articleInfo.getUserId());
        vo.setArticleInfo(articleInfo);
        return vo;
    }

    default ArticleVo toArticleVo(Article articleInfo, String content) {
        if (articleInfo == null) return null;
        // set content
        articleInfo.setContent(content);
        ArticleVo vo = new ArticleVo();

        vo.setId(articleInfo.getId());
        vo.setUserId(articleInfo.getUserId());
        vo.setArticleInfo(articleInfo);
        return vo;
    }


    default List<ArticleVo> toArticleVos(Collection<Article> articleInfos) {
        if (articleInfos == null || articleInfos.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<ArticleVo> dtos = new ArrayList<>(articleInfos.size());
        for (Article articleInfo : articleInfos) {
            ArticleVo dto = toArticleVo(articleInfo);
            dtos.add(dto);
        }
        return dtos;
    }


}
