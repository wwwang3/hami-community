package top.wang3.hami.common.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import top.wang3.hami.common.dto.*;
import top.wang3.hami.common.dto.request.ArticleDraftParam;
import top.wang3.hami.common.model.*;

import java.util.ArrayList;
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

    @Mapping(target = "tagName", source = "name")
    @Mapping(target = "tagId", source = "id")
    TagDTO toTagDTO(Tag tag);

    ArticleDraftDTO toDraftDTO(ArticleDraft draft, List<Tag> tags);

    @Mapping(target = "categoryId", source = "id")
    @Mapping(target = "categoryName", source = "name")
    CategoryDTO toCategoryDTO(Category category);

    ArticleStatDTO toArticleStatDTO(ArticleStat stat);
    List<ArticleStatDTO> toArticleStatDTOList(List<ArticleStat> stat);

    List<ReadingRecordDTO> toReadingRecordDTO(List<ReadingRecord> records);

    ArticleInfo toArticleInfo(Article article, List<Integer> tagIds);

    default ArticleDTO toArticleDTO(ArticleInfo articleInfo) {
        if (articleInfo == null) return null;
        ArticleDTO articleDTO = new ArticleDTO();
        articleDTO.setId(articleInfo.getId());
        articleDTO.setUserId(articleInfo.getUserId());
        articleDTO.setArticleInfo(articleInfo);
        return articleDTO;
    }

    default List<ArticleDTO> toArticleDTOS(List<ArticleInfo> articleInfos) {
        if (articleInfos == null || articleInfos.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<ArticleDTO> dtos = new ArrayList<>(articleInfos.size());
        for (ArticleInfo articleInfo : articleInfos) {
            ArticleDTO dto = toArticleDTO(articleInfo);
            dtos.add(dto);
        }
        return dtos;
    }

    default ArticleContentDTO toArticleContentDTO(ArticleInfo articleInfo, String content) {
        if (articleInfo == null) return null;
        ArticleContentDTO articleDTO = new ArticleContentDTO();
        articleDTO.setId(articleInfo.getId());
        articleDTO.setUserId(articleInfo.getUserId());
        articleDTO.setArticleInfo(articleInfo);
        articleDTO.setContent(content);
        return articleDTO;
    }
}
