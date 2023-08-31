package top.wang3.hami.common.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import top.wang3.hami.common.dto.ArticleDraftDTO;
import top.wang3.hami.common.dto.TagDTO;
import top.wang3.hami.common.dto.request.ArticleDraftParam;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.model.ArticleDraft;
import top.wang3.hami.common.model.Tag;

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

    @Mapping(target = "tagName", source = "name")
    @Mapping(target = "tagId", source = "id")
    TagDTO toTagDTO(Tag tag);

    ArticleDraftDTO toDraftDTO(ArticleDraft draft, List<Tag> tags);
}
