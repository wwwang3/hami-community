package top.wang3.hami.common.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import top.wang3.hami.common.dto.*;
import top.wang3.hami.common.dto.request.ArticleDraftParam;
import top.wang3.hami.common.model.*;

import java.util.ArrayList;
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

    default List<ArticleDTO> toArticleDTOList(List<Article> articles) {
        if (articles == null) return null;
        ArrayList<ArticleDTO> list = new ArrayList<>(articles.size());
        for (Article article : articles) {
            list.add(toArticleDTO(article));
        }
        return list;
    }

    @Mappings(value = {
            @Mapping(target = "collected", ignore = true),
            @Mapping(target = "category", ignore = true),
            @Mapping(target = "author", ignore = true),
            @Mapping(target = "liked", ignore = true),
            @Mapping(target = "stat", ignore = true),
            @Mapping(target = "tags", ignore = true)
    })
    ArticleDTO toArticleDTO(Article article);

    @Mapping(target = "categoryId", source = "id")
    @Mapping(target = "categoryName", source = "name")
    CategoryDTO toCategoryDTO(Category category);

    ArticleStatDTO toArticleStatDTO(ArticleStat stat);
    List<ArticleStatDTO> toArticleStatDTOList(List<ArticleStat> stat);
}
