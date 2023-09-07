package top.wang3.hami.core.service.article.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.converter.ArticleConverter;
import top.wang3.hami.common.dto.*;
import top.wang3.hami.common.dto.request.ArticlePageParam;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.mapper.ArticleMapper;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.article.ArticleStatService;
import top.wang3.hami.core.service.article.ArticleTagService;
import top.wang3.hami.core.service.article.CategoryService;
import top.wang3.hami.core.service.common.CountService;
import top.wang3.hami.core.service.common.UserInteractService;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article>
        implements ArticleService {


    private final String[] fields =  {"id", "user_id", "title", "summary", "picture", "category_id", "ctime", "mtime"};

    private final ArticleTagService articleTagService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final UserInteractService userInteractService;
    private final CountService countService;
    private final ArticleStatService articleStatService;

    @Override
    public PageData<ArticleDTO> listNewestArticles(ArticlePageParam param) {
        Integer cateId = param.getCateId();
        Integer tagId = param.getTagId();
        Page<Article> page = param.toPage();
        List<Article> articles;
        //查询文章列表
        if (tagId != null) {
            articles = listArticleByCateAndTag(page, cateId, tagId);
        } else {
            articles = listArticleByCate(page, cateId);
        }
        List<ArticleDTO> articleDTOS = ArticleConverter.INSTANCE.toArticleDTOList(articles);
        articles = null;
        List<Integer> articleIds = ListMapperHandler.listTo(articleDTOS, ArticleDTO::getId);
        List<Integer> userIds = ListMapperHandler.listTo(articleDTOS, ArticleDTO::getUserId);
        //查询文章分类
        buildCategory(articleDTOS);
        //查询文章标签
        buildArticleTags(articleDTOS, articleIds);
        //查询作者信息
        buildArticleAuthor(articleDTOS, userIds);
        //查询文章数据
        buildArticleStat(articleDTOS, articleIds);
        //查询用户行为(点赞，收藏)
        buildInteract(articleDTOS, articleIds);
        return PageData.<ArticleDTO>builder()
                .total(page.getTotal())
                .pageNum(page.getCurrent())
                .pageSize(page.getSize())
                .data(articleDTOS)
                .build();
    }

    private List<Article> listArticleByCate(Page<Article> page, Integer cateId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select(fields)
                .eq(cateId != null, "category_id", cateId) //创建时间正常情况下都会递增
                .list(page);
    }

    private List<Article> listArticleByCateAndTag(Page<Article> page, Integer cateId, Integer tagId) {
        return getBaseMapper().selectArticlesByCateIdAndTag(page, cateId, tagId);
    }

    public void buildCategory(List<ArticleDTO> dtos) {
        dtos.forEach(t -> {
            CategoryDTO categoryDTO = categoryService.getCategoryDTOById(t.getCategoryId());
            t.setCategory(categoryDTO);
        });
    }

    public void buildArticleTags(List<ArticleDTO> dtos, List<Integer> articleIds) {
        List<ArticleTagDTO> tags = articleTagService.listArticleTagByArticleIds(articleIds);
        ListMapperHandler.doAssemble(dtos, ArticleDTO::getId, tags, ArticleTagDTO::getArticleId,
                (d, t) -> d.setTags(t != null ? t.getTags() : null));
    }

    public void buildArticleStat(List<ArticleDTO> dtos, List<Integer> articleIds) {
        //查询文章数据
//        dtos.forEach(dto -> {
//            ArticleStatDTO stat = countService.getArticleStatById(dto.getId());
//            dto.setStat(stat);
//        });
        List<ArticleStatDTO> stats = articleStatService.getArticleStatByArticleIds(articleIds);
        ListMapperHandler.doAssemble(dtos, ArticleDTO::getId, stats,
                ArticleStatDTO::getArticleId, ArticleDTO::setStat);
    }

    public void buildArticleAuthor(List<ArticleDTO> dtos, List<Integer> userIds) {
        List<SimpleUserDTO> authors = userService.getAuthorInfoByIds(userIds);
        ListMapperHandler
                .doAssemble(dtos, ArticleDTO::getUserId, authors, SimpleUserDTO::getUserId, ArticleDTO::setAuthor);
    }

    public void buildInteract(List<ArticleDTO> dtos, List<Integer> articleIds) {
        //是否点赞
        //是否收藏
        Integer loginUserId = LoginUserContext.getLoginUserIdDefaultNull();
        if (loginUserId == null) return;
        Map<Integer, Boolean> liked =
                userInteractService.hasLiked(loginUserId, articleIds, Constants.LIKE_TYPE_ARTICLE);
        ListMapperHandler.doAssemble(dtos, ArticleDTO::getId, liked, ArticleDTO::setLiked);
        Map<Integer, Boolean> collected =
                userInteractService.hasCollected(loginUserId, articleIds, Constants.LIKE_TYPE_ARTICLE);
        ListMapperHandler.doAssemble(dtos, ArticleDTO::getId, collected, ArticleDTO::setCollected);
    }
}
