package top.wang3.hami.common.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import top.wang3.hami.common.dto.request.ArticleDraftParam;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.model.ArticleDraft;

@Mapper
public interface ArticleConverter {

    ArticleConverter INSTANCE = Mappers.getMapper(ArticleConverter.class);

    @Mappings(value = {
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "state", ignore = true),
            @Mapping(target = "mtime", ignore = true),
            @Mapping(target = "deleted", ignore = true),
            @Mapping(target = "ctime", ignore = true),
            @Mapping(target = "ctime", ignore = true),
            @Mapping(target = "articleId", ignore = true)
    })
    ArticleDraft toArticleDraft(ArticleDraftParam param);


    @Mapping(target = "id", source = "articleId")
    Article toArticle(ArticleDraft draft);
}
