package top.wang3.hami.common.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.dto.article.ArticleDraftParam;
import top.wang3.hami.common.dto.article.ArticleInfo;
import top.wang3.hami.common.dto.article.CategoryDTO;
import top.wang3.hami.common.model.*;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.vo.article.ArticleContentVo;
import top.wang3.hami.common.vo.article.ArticleDTO;
import top.wang3.hami.common.vo.article.ArticleDraftVo;

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


    ArticleDraftVo toDraftDTO(ArticleDraft draft, List<Tag> tags);

    @Mapping(target = "categoryId", source = "id")
    @Mapping(target = "categoryName", source = "name")
    CategoryDTO toCategoryDTO(Category category);


    default ArticleDTO toArticleDTO(ArticleInfo articleInfo) {
        if (articleInfo == null) return null;
        ArticleDTO articleDTO = new ArticleDTO();
        articleDTO.setId(articleInfo.getId());
        articleDTO.setUserId(articleInfo.getUserId());
        articleDTO.setArticleInfo(articleInfo);
        return articleDTO;
    }

    default List<ArticleDTO> toArticleDTOS(Collection<ArticleInfo> articleInfos) {
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

    default ArticleContentVo toArticleContentDTO(ArticleInfo info, String content) {
        if (info == null && content == null) {
            return null;
        }
        ArticleContentVo dto = new ArticleContentVo();
        if (info != null) {
            dto.setArticleInfo(info);
            dto.setId(info.getId());
            dto.setUserId(info.getUserId());
        }
        dto.setContent(content);
        return dto;
    }


    ArticleInfo toArticleInfo(ArticleDO articleDO, Collection<Integer> tagIds);

    default List<ArticleInfo> toArticleInfos(List<ArticleDO> dos) {
        if (CollectionUtils.isEmpty(dos)) {
            return Collections.emptyList();
        }
        ArrayList<ArticleInfo> articleDTOS = new ArrayList<>(dos.size());
        for (ArticleDO item : dos) {
            Collection<Integer> tagIds = ListMapperHandler.listTo(item.getTags(), ArticleTag::getTagId);
            articleDTOS.add(toArticleInfo(item, tagIds));
        }
        return articleDTOS;
    }
}
