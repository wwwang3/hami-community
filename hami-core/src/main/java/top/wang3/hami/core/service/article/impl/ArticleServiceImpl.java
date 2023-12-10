package top.wang3.hami.core.service.article.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.converter.ArticleConverter;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.dto.article.*;
import top.wang3.hami.common.dto.builder.ArticleOptionsBuilder;
import top.wang3.hami.common.dto.builder.UserOptionsBuilder;
import top.wang3.hami.common.dto.interact.LikeType;
import top.wang3.hami.common.dto.user.UserDTO;
import top.wang3.hami.common.message.ArticleRabbitMessage;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RandomUtils;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.component.ZPageHandler;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.article.CategoryService;
import top.wang3.hami.core.service.article.TagService;
import top.wang3.hami.core.service.article.repository.ArticleRepository;
import top.wang3.hami.core.service.interact.CollectService;
import top.wang3.hami.core.service.interact.LikeService;
import top.wang3.hami.core.service.stat.CountService;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.security.context.IpContext;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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


    @CostLog
    @Override
    public PageData<ArticleDTO> listNewestArticles(ArticlePageParam param) {
        Integer cateId = param.getCateId();
        Page<Article> page = param.toPage(false);
        //查询文章列表
        List<ArticleDTO> articleDTOS = this.listArticleByCate(page, cateId);
        return PageData.<ArticleDTO>builder()
                .total(page.getTotal())
                .pageNum(page.getCurrent())
                .data(articleDTOS)
                .build();
    }

    @CostLog
    @Override
    public PageData<ArticleDTO> listUserArticle(UserArticleParam param) {
        //获取用户文章
        int userId = param.getUserId();
        String redisKey = RedisConstants.LIST_USER_ARTICLE + userId;
        Page<Article> page = param.toPage(false);
        Collection<Integer> ids = ZPageHandler.<Integer>of(redisKey, page, this)
                .countSupplier(() -> this.getUserArticleCount(userId))
                .source((c, s) -> {
                    Page<Article> articlePage = new Page<>(c, s, false);
                    return articleRepository.listArticleByPage(articlePage, null, userId);
                })
                .loader((c, s) -> {
                    return loadUserArticleListCache(redisKey, userId, c, s);
                }).query();
        List<ArticleDTO> dtos = this.listArticleDTOById(ids, new ArticleOptionsBuilder());
        return PageData.<ArticleDTO>builder()
                .total(page.getTotal())
                .data(dtos)
                .pageNum(page.getCurrent())
                .build();
    }

    @CostLog
    @Override
    public PageData<ArticleDTO> listFollowUserArticles(PageParam param) {
        //获取关注用户的最新文章
        int loginUserId = LoginUserContext.getLoginUserId();
        Page<Article> page = param.toPage();
        List<Integer> articleIds = articleRepository.listFollowUserArticles(page, loginUserId);
        List<ArticleDTO> dtos = this.listArticleDTOById(articleIds, null);
        return PageData.<ArticleDTO>builder()
                .pageNum(page.getCurrent())
                .data(dtos)
                .total(page.getTotal())
                .build();
    }

    @NonNull
    @Override
    public Long getArticleCount(Integer cateId) {
        String redisKey = cateId == null ? RedisConstants.TOTAL_ARTICLE_COUNT : RedisConstants.CATE_ARTICLE_COUNT + cateId;
        Long count = RedisClient.getCacheObject(redisKey);
        if (count == null) {
            synchronized (this) {
                count = RedisClient.getCacheObject(redisKey);
                if (count == null) {
                    count = articleRepository.getArticleCount(cateId, null);
                }
                RedisClient.setCacheObject(redisKey, count, RandomUtils.randomLong(20, 30), TimeUnit.DAYS);
            }
        }
        return count;
    }

    @NonNull
    @Override
    public Long getUserArticleCount(Integer userId) {
        Assert.notNull(userId, "userId  can not be null");
        String redisKey = RedisConstants.USER_ARTICLE_COUNT + userId;
        Long count = RedisClient.getCacheObject(redisKey);
        if (count == null) {
            synchronized (this) {
                count = RedisClient.getCacheObject(redisKey);
                if (count == null) {
                    count = articleRepository.getArticleCount(null, userId);
                }
                RedisClient.setCacheObject(redisKey, count, RandomUtils.randomLong(20, 30), TimeUnit.DAYS);
            }
        }
        return count;
    }

    private List<ArticleDTO> listArticleByCate(Page<Article> page, Integer cateId) {
        String key = cateId == null ? RedisConstants.ARTICLE_LIST : RedisConstants.CATE_ARTICLE_LIST + cateId;
        Collection<Integer> articleIds = ZPageHandler.<Integer>of(key, page, this)
                .countSupplier(() -> this.getArticleCount(cateId))
                .source((current, size) -> {
                    Page<Article> itemPage = new Page<>(current, size, false);
                    return articleRepository.listArticleByPage(itemPage, cateId, null);
                })
                .loader((current, size) -> {
                    return loadArticleListCache(key, cateId, current, size);
                })
                .query();
        return this.listArticleDTOById(articleIds, new ArticleOptionsBuilder());
    }

    @Override
    public List<Integer> loadArticleListCache(String key, Integer cateId, long current, long size) {
        List<Article> articles = articleRepository.listArticleByCateId(cateId);
        return cacheArticleListToRedis(key, current, size, articles);
    }

    @Override
    public List<Integer> loadUserArticleListCache(String key, Integer userId, long current, long size) {
        List<Article> articles = articleRepository.listUserArticle(userId);
        return cacheArticleListToRedis(key, current, size, articles);
    }

    @Override
    public ArticleContentDTO getArticleContentById(int articleId) {
        ArticleInfo info = this.getArticleInfoById(articleId);
        if (info == null || info.getId() == null || info.getDeleted() == Constants.DELETED) {
            return null;
        }
        String content = this.loadArticleContent(articleId);
        //no tag, category, author...
        ArticleContentDTO dto = ArticleConverter.INSTANCE.toArticleContentDTO(info, content);

        //build
        List<ArticleContentDTO> dtos = List.of(dto);
        this.buildCategory(dtos);
        this.buildArticleTags(dtos);
        //作者信息, 包含用户数据
        UserDTO author = userService.getAuthorInfoById(dto.getUserId());
        dto.setAuthor(author);
        //文章数据
        ArticleStatDTO stat = countService.getArticleStatById(articleId);
        dto.setStat(stat);
        //用户行为
        buildInteract(dto);
        //增加views
        this.record(LoginUserContext.getLoginUserIdDefaultNull(), articleId, dto.getUserId());
        return dto;
    }

    private void record(Integer loginUserId, int articleId, int authorId) {
        //记录阅读数据
        String ip = IpContext.getIp();
        if (ip == null) return;
        String redisKey = RedisConstants.VIEW_LIMIT + ip + ":" + articleId;
        boolean success = RedisClient.setNx(redisKey, "view-lock", 15, TimeUnit.SECONDS);
        if (!success) {
            log.debug("ip: {} access repeat", ip);
            return;
        }
        //发布消息
        ArticleRabbitMessage message = new ArticleRabbitMessage(ArticleRabbitMessage.Type.VIEW,
                articleId, authorId, loginUserId);
        rabbitMessagePublisher.publishMsg(message);
    }


    @Override
    public List<ArticleDTO> listArticleDTOById(Collection<Integer> ids, ArticleOptionsBuilder builder) {
        if (CollectionUtils.isEmpty(ids)) return Collections.emptyList();
        List<ArticleInfo> infos = this.listArticleInfoById(ids);
        List<ArticleDTO> dtos = ArticleConverter.INSTANCE.toArticleDTOS(infos);
        this.buildArticleDTOs(dtos, builder);
        return dtos;
    }

    private ArticleInfo getArticleInfoById(Integer id) {
        String key = RedisConstants.ARTICLE_INFO + id;
        ArticleInfo info = RedisClient.getCacheObject(key);
        if (info != null && info.getId() == null) {
            return null;
        } else if (info == null) {
            synchronized (this) {
                info = RedisClient.getCacheObject(key);
                if (info != null) return info;
                //load cache
                info = this.loadArticleInfoFromDB(id);
                if (info == null) {
                    RedisClient.cacheEmptyObject(key, new ArticleInfo());
                } else {
                    RedisClient.setCacheObject(key, info, RandomUtils.randomLong(10, 20), TimeUnit.DAYS);
                }
            }
        }
        return info;
    }

    private List<ArticleInfo> listArticleInfoById(Collection<Integer> ids) {
        //fix? 感觉还是单个单个获取比较好
        return RedisClient.getMultiCacheObject(RedisConstants.ARTICLE_INFO, ids, this::loadArticleInfoCache);
    }

    @Override
    public boolean saveArticle(Article article) {
        return articleRepository.saveArticle(article);
    }

    @Override
    public boolean updateArticle(Article article) {
        return articleRepository.updateArticle(article);
    }

    @Override
    public boolean deleteByArticleId(Integer articleId, Integer userId) {
        return articleRepository.deleteArticle(articleId, userId);
    }

    private ArticleInfo loadArticleInfoFromDB(Integer id) {
        return articleRepository.getArticleInfoById(id);
    }

    @Override
    public List<ArticleInfo> loadArticleInfoCache(List<Integer> nullIds) {
        //根据ID批量获取,
        List<ArticleInfo> infos = articleRepository.listArticleById(nullIds);
        RedisClient.cacheMultiObject(infos, a -> RedisConstants.ARTICLE_INFO + a.getId(), 10, 100, TimeUnit.HOURS);
        return infos;
    }



    private String loadArticleContent(Integer articleId) {
        String key = RedisConstants.ARTICLE_CONTENT + articleId;
        String content = RedisClient.getCacheObject(key);
        if (content == null || content.isEmpty()) {
            //文章内容不会为空
            synchronized (this) {
                content = RedisClient.getCacheObject(key);
                if (content == null || content.isEmpty()) {
                    content = articleRepository.getArticleContentById(articleId);
                    RedisClient.setCacheObject(key, content, 24L + RandomUtils.randomLong(1, 20), TimeUnit.HOURS);
                }
            }
        }
        return content;
    }

    public void buildArticleDTOs(List<ArticleDTO> articleDTOS,
                                 ArticleOptionsBuilder builder) {
        List<Integer> articleIds = ListMapperHandler.listTo(articleDTOS, ArticleDTO::getId);
        List<Integer> userIds = ListMapperHandler.listTo(articleDTOS, ArticleDTO::getUserId);
        if (builder == null) {
            builder = new ArticleOptionsBuilder();
        }
        //分类
        this.buildCategory(articleDTOS);
        //标签
        this.buildArticleTags(articleDTOS);
        //查询作者信息
        builder.ifAuthor(() -> this.buildArticleAuthor(articleDTOS, userIds));
        //查询文章数据
        builder.ifStat(() -> this.buildArticleStat(articleDTOS, articleIds));
        //查询用户行为(点赞，收藏)
        builder.ifInteract(() -> this.buildInteract(articleDTOS, articleIds));
    }

    public void buildCategory(List<? extends ArticleDTO> dtos) {
        dtos.forEach(t -> {
            ArticleInfo info = t.getArticleInfo();
            CategoryDTO categoryDTO = categoryService.getCategoryDTOById(info.getCategoryId());
            t.setCategory(categoryDTO);
        });
    }

    public void buildArticleTags(List<? extends ArticleDTO> dtos) {
        dtos.forEach(dto -> {
            ArticleInfo info = dto.getArticleInfo();
            List<Integer> ids = info.getTagIds();
            List<TagDTO> tags = tagService.getTagDTOsByIds(ids);
            dto.setTags(tags);
        });
    }

    public void buildArticleStat(List<ArticleDTO> dtos, List<Integer> articleIds) {
        Map<Integer, ArticleStatDTO> stats = countService.getArticleStatByIds(articleIds);
        ListMapperHandler.doAssemble(dtos, ArticleDTO::getId, stats, ArticleDTO::setStat);
    }

    public void buildArticleAuthor(List<ArticleDTO> dtos, Collection<Integer> userIds) {
        //文章列表不查询用户数据
        UserOptionsBuilder builder = new UserOptionsBuilder()
                .noStat();
        Collection<UserDTO> authors = userService.listAuthorInfoById(userIds, builder);
        ListMapperHandler
                .doAssemble(dtos, ArticleDTO::getUserId, authors, UserDTO::getUserId, ArticleDTO::setAuthor);
    }

    public void buildInteract(List<ArticleDTO> dtos, List<Integer> articleIds) {
        //是否点赞
        //是否收藏
        Integer loginUserId = LoginUserContext.getLoginUserIdDefaultNull();
        if (loginUserId == null) return;
        Map<Integer, Boolean> liked =
                likeService.hasLiked(loginUserId, articleIds, LikeType.ARTICLE);
        ListMapperHandler.doAssemble(dtos, ArticleDTO::getId, liked, ArticleDTO::setLiked);
        Map<Integer, Boolean> collected =
                collectService.hasCollected(loginUserId, articleIds);
        ListMapperHandler.doAssemble(dtos, ArticleDTO::getId, collected, ArticleDTO::setCollected);
    }

    private void buildInteract(final ArticleDTO articleDTO) {
        Integer loginUserId = LoginUserContext.getLoginUserIdDefaultNull();
        if (loginUserId == null) {
            return;
        }
        boolean liked = likeService.hasLiked(loginUserId, articleDTO.getId(), LikeType.ARTICLE);
        boolean collected = collectService.hasCollected(loginUserId, articleDTO.getId());
        articleDTO.setLiked(liked);
        articleDTO.setCollected(collected);
    }

    private static List<Integer> cacheArticleListToRedis(String key, long current, long size, List<Article> articles) {
        Collection<Tuple> tuples = ListMapperHandler.listToTuple(articles, Article::getId, article -> article.getCtime().getTime());
        RedisClient.zSetAll(key, tuples, RandomUtils.randomLong(100, 200), TimeUnit.HOURS);
        return ListMapperHandler.subList(articles, Article::getId, current, size);
    }
}
