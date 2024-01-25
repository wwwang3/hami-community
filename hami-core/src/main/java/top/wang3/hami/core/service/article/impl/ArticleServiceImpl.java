package top.wang3.hami.core.service.article.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.converter.ArticleConverter;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.dto.article.ArticlePageParam;
import top.wang3.hami.common.dto.article.UserArticleParam;
import top.wang3.hami.common.dto.builder.ArticleOptionsBuilder;
import top.wang3.hami.common.dto.builder.UserOptionsBuilder;
import top.wang3.hami.common.dto.interact.LikeType;
import top.wang3.hami.common.dto.stat.ArticleStatDTO;
import top.wang3.hami.common.message.ArticleRabbitMessage;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.model.Category;
import top.wang3.hami.common.model.Tag;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.common.vo.article.ArticleVo;
import top.wang3.hami.common.vo.user.UserVo;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.article.CategoryService;
import top.wang3.hami.core.service.article.TagService;
import top.wang3.hami.core.service.article.cache.ArticleCacheService;
import top.wang3.hami.core.service.article.repository.ArticleRepository;
import top.wang3.hami.core.service.interact.CollectService;
import top.wang3.hami.core.service.interact.LikeService;
import top.wang3.hami.core.service.stat.CountService;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.security.context.IpContext;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleServiceImpl implements ArticleService {

    private final CategoryService categoryService;
    private final UserService userService;
    private final LikeService likeService;
    private final CollectService collectService;

    private final CountService countService;
    private final ArticleRepository articleRepository;
    private final TagService tagService;
    private final RabbitMessagePublisher rabbitMessagePublisher;

    private final ArticleCacheService articleCacheService;

    private final ThreadPoolTaskExecutor executor;


    @Override
    public PageData<ArticleVo> listNewestArticles(ArticlePageParam param) {
        Integer cateId = param.getCateId();
        Page<Article> page = param.toPage(false);
        // 查询文章ID列表
        List<Integer> articleIds = articleCacheService.listArticleIdByPage(page, cateId);
        // 获取article-vo
        List<ArticleVo> articleVos = this.listArticleVoById(articleIds, new ArticleOptionsBuilder());
        return PageData.<ArticleVo>builder()
                .total(page.getTotal())
                .pageNum(page.getCurrent())
                .data(articleVos)
                .build();
    }

    @Override
    public PageData<ArticleVo> listUserArticles(UserArticleParam param) {
        //获取用户文章
        int userId = param.getUserId();
        Page<Article> page = param.toPage(false);
        // 获取文章ID列表
        List<Integer> articleIds = articleCacheService.listUserArticleIdByPage(page, userId);
        // 获取vo
        List<ArticleVo> dtos = this.listArticleVoById(articleIds, new ArticleOptionsBuilder());
        return PageData.<ArticleVo>builder()
                .total(page.getTotal())
                .data(dtos)
                .pageNum(page.getCurrent())
                .build();
    }

    @Override
    public PageData<ArticleVo> listFollowUserArticles(PageParam param) {
        //获取关注用户的最新文章
        int loginUserId = LoginUserContext.getLoginUserId();
        Page<Article> page = param.toPage();
        // 从数据库获取
        List<Integer> articleIds = articleRepository.listFollowUserArticles(page, loginUserId);
        // 获取vo
        List<ArticleVo> dtos = this.listArticleVoById(articleIds, null);
        return PageData.<ArticleVo>builder()
                .pageNum(page.getCurrent())
                .data(dtos)
                .total(page.getTotal())
                .build();
    }

    @Override
    public List<ArticleVo> listArticleVoById(List<Integer> ids, ArticleOptionsBuilder builder) {
        if (CollectionUtils.isEmpty(ids)) return Collections.emptyList();
        // 从缓存中获取
        List<Article> infos = articleCacheService.listArticleInfoById(ids);
        // 转化为Vo
        List<ArticleVo> dtos = ArticleConverter.INSTANCE.toArticleVos(infos);
        this.buildArticleVos(dtos, builder);
        return dtos;
    }

    @Override
    public ArticleVo getArticleContentById(int articleId) {
        Article info = articleCacheService.getArticleInfoCache(articleId);
        if (info == null || info.getId() == null || Objects.equals(info.getDeleted(), Constants.DELETED)) {
            return null;
        }
        String content = articleCacheService.getArticleContentCache(articleId);
        ArticleVo vo = ArticleConverter.INSTANCE.toArticleVo(info, content);
        // build category, tag, author, interact
        List<ArticleVo> dtos = List.of(vo);
        this.buildCategory(dtos);
        this.buildArticleTags(dtos);
        // 作者信息, 包含用户数据
        UserVo author = userService.getAuthorInfoById(vo.getUserId());
        vo.setAuthor(author);
        // 文章数据
        ArticleStatDTO stat = countService.getArticleStatById(articleId);
        vo.setStat(stat);
        // 用户行为
        buildInteract(vo);
        // 增加文章阅读量, 用户阅读记录
        executor.execute(() -> record(articleId, author.getUserId()));
        return vo;
    }

    private void record(int articleId, int authorId) {
        // 记录阅读数据
        String ip = IpContext.getIp();
        if (ip == null) return;
        String redisKey = RedisConstants.VIEW_LIMIT + ip + ":" + articleId;
        boolean success = RedisClient.setNx(redisKey, "view-lock", 15, TimeUnit.SECONDS);
        if (!success) {
            log.debug("ip: {} access repeat", ip);
            return;
        }
        // 发布消息
        ArticleRabbitMessage message = new ArticleRabbitMessage(ArticleRabbitMessage.Type.VIEW,
                articleId, authorId, LoginUserContext.getLoginUserIdDefaultNull());
        rabbitMessagePublisher.publishMsg(message);
    }

    public void buildArticleVos(List<ArticleVo> articleVos,
                                 ArticleOptionsBuilder builder) {
        List<Integer> articleIds = ListMapperHandler.listTo(articleVos, ArticleVo::getId);
        List<Integer> userIds = ListMapperHandler.listTo(articleVos, ArticleVo::getUserId);
        if (builder == null) {
            builder = new ArticleOptionsBuilder();
        }
        // 分类
        this.buildCategory(articleVos);
        // 标签
        this.buildArticleTags(articleVos);
        // 查询作者信息
        builder.ifAuthor(() -> this.buildArticleAuthor(articleVos, userIds));
        // 查询文章数据
        builder.ifStat(() -> this.buildArticleStat(articleVos, articleIds));
        // 查询用户行为(点赞，收藏)
        builder.ifInteract(() -> this.buildInteract(articleVos, articleIds));
    }

    public void buildCategory(List<? extends ArticleVo> dtos) {
        for (ArticleVo vo : dtos) {
            Integer cateId = vo.getArticleInfo().getCategoryId();
            Category category = categoryService.getCategoryById(cateId);
            vo.setCategory(category);
        }
    }

    public void buildArticleTags(List<? extends ArticleVo> vos) {
        for (ArticleVo vo : vos) {
            Article info = vo.getArticleInfo();
            List<Integer> ids = info.getTagIds();
            List<Tag> tags = tagService.getTagsByIds(ids);
            vo.setTags(tags);
        }
    }

    public void buildArticleStat(List<ArticleVo> vos, List<Integer> articleIds) {
        Map<Integer, ArticleStatDTO> stats = countService.getArticleStatByIds(articleIds);
        ListMapperHandler.doAssemble(vos, ArticleVo::getId, stats, ArticleVo::setStat);
    }

    public void buildArticleAuthor(List<ArticleVo> dtos, List<Integer> userIds) {
        // 文章列表不查询用户数据
        UserOptionsBuilder builder = new UserOptionsBuilder()
                .noStat();
        List<UserVo> authors = userService.listAuthorById(userIds, builder);
        ListMapperHandler.doAssemble(
                dtos,
                ArticleVo::getUserId,
                authors,
                UserVo::getUserId,
                ArticleVo::setAuthor
        );
    }

    public void buildInteract(List<ArticleVo> dtos, List<Integer> articleIds) {
        // 是否点赞
        // 是否收藏
        Integer loginUserId = LoginUserContext.getLoginUserIdDefaultNull();
        if (loginUserId == null) return;
        Map<Integer, Boolean> liked =
                likeService.hasLiked(loginUserId, articleIds, LikeType.ARTICLE);
        ListMapperHandler.doAssemble(dtos, ArticleVo::getId, liked, ArticleVo::setLiked);
        Map<Integer, Boolean> collected =
                collectService.hasCollected(loginUserId, articleIds);
        ListMapperHandler.doAssemble(dtos, ArticleVo::getId, collected, ArticleVo::setCollected);
    }

    private void buildInteract(final ArticleVo articleVo) {
        Integer loginUserId = LoginUserContext.getLoginUserIdDefaultNull();
        if (loginUserId == null) {
            return;
        }
        boolean liked = likeService.hasLiked(loginUserId, articleVo.getId(), LikeType.ARTICLE);
        boolean collected = collectService.hasCollected(loginUserId, articleVo.getId());
        articleVo.setLiked(liked);
        articleVo.setCollected(collected);
    }
}
