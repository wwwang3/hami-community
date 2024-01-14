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
import top.wang3.hami.common.vo.article.ArticleDraftVo;
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


    ArticleDraftVo toDraftDTO(ArticleDraft draft, List<Tag> tags);

    @Mapping(target = "categoryId", source = "id")
    @Mapping(target = "categoryName", source = "name")
    CategoryDTO toCategoryDTO(Category category);


    default ArticleVo toArticleVo(ArticleInfo articleInfo) {
        if (articleInfo == null) return null;
        ArticleVo articleDTO = new ArticleVo();

        articleDTO.setId(articleInfo.getId());
        articleDTO.setUserId(articleInfo.getUserId());
        articleDTO.setArticleInfo(articleInfo);
        return articleDTO;
    }

    default List<ArticleVo> toArticleVos(Collection<ArticleInfo> articleInfos) {
        if (articleInfos == null || articleInfos.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<ArticleVo> dtos = new ArrayList<>(articleInfos.size());
        for (ArticleInfo articleInfo : articleInfos) {
            ArticleVo dto = toArticleVo(articleInfo);
            dtos.add(dto);
        }
        return dtos;
    }

    default ArticleContentVo toArticleContentVo(ArticleInfo info, String content) {
        if (info == null && content == null) {
            return null;
        }
        ArticleContentVo vo = new ArticleContentVo();
        if (info != null) {
            vo.setArticleInfo(info);
            vo.setId(info.getId());
            vo.setUserId(info.getUserId());
        }
        vo.setContent(content);
        return vo;
    }


    ArticleInfo toArticleInfo(ArticleDO articleDO, Collection<Integer> tagIds);

    default List<ArticleInfo> toArticleInfos(List<ArticleDO> dos) {
        if (CollectionUtils.isEmpty(dos)) {
            return Collections.emptyList();
        }
        ArrayList<ArticleInfo> dtos = new ArrayList<>(dos.size());
        for (ArticleDO item : dos) {
            Collection<Integer> tagIds = ListMapperHandler.listTo(item.getTags(), ArticleTag::getTagId);
            dtos.add(toArticleInfo(item, tagIds));
        }
        return dtos;
    }
}
