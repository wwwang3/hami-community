package top.wang3.hami.core.service.article.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.converter.ArticleConverter;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.article.*;
import top.wang3.hami.common.dto.builder.ArticleOptionsBuilder;
import top.wang3.hami.common.dto.builder.UserOptionsBuilder;
import top.wang3.hami.common.dto.request.ArticlePageParam;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.dto.request.UserArticleParam;
import top.wang3.hami.common.dto.user.UserDTO;
import top.wang3.hami.common.enums.LikeType;
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
        String redisKey = Constants.LIST_USER_ARTICLE + userId;
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
        List<ArticleDTO> dtos = this.listArticleById(ids, new ArticleOptionsBuilder());
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
        List<ArticleDTO> dtos = this.listArticleById(articleIds, null);
        return PageData.<ArticleDTO>builder()
                .pageNum(page.getCurrent())
                .data(dtos)
                .total(page.getTotal())
                .build();
    }

    @NonNull
    @Override
    public Long getArticleCount(Integer cateId) {
        String redisKey = cateId == null ? Constants.TOTAL_ARTICLE_COUNT : Constants.CATE_ARTICLE_COUNT + cateId;
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
        String redisKey = Constants.USER_ARTICLE_COUNT + userId;
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
        String key = cateId == null ? Constants.ARTICLE_LIST : Constants.CATE_ARTICLE_LIST + cateId;
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
        return this.listArticleById(articleIds, new ArticleOptionsBuilder());
    }

    @Override
    public List<Integer> loadArticleListCache(String key,  Integer cateId, long current, long size) {
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
        ArticleDTO article = this.getArticleDTOById(articleId);
        if (article == null || article.getId() == null) return null;

        String content = loadArticleContent(articleId);
        ArticleContentDTO dto = ArticleConverter.INSTANCE.toArticleContentDTO(article, content);

        //作者信息, 包含用户数据
        UserDTO author = userService.getAuthorInfoById(dto.getUserId());
        dto.setAuthor(author);
        //文章数据
        ArticleStatDTO stat = countService.getArticleStatById(articleId);
        dto.setStat(stat);

        //增加views
        int record = this.record(LoginUserContext.getLoginUserIdDefaultNull(), articleId, dto.getUserId());
        dto.getStat().setViews(stat.getViews() + record);
        Integer totalViews = dto.getAuthor().getStat().getTotalViews();
        dto.getAuthor().getStat().setTotalViews(totalViews + record);

        //用户行为
        buildInteract(dto);
        return dto;
    }

    private int record(Integer loginUserId, int articleId, int authorId) {
        //记录阅读数据
        String ip = IpContext.getIp();
        if (ip == null) return 0;
        String redisKey = Constants.VIEW_LIMIT + ip + ":" + articleId;
        boolean success = RedisClient.setNx(redisKey, "view-lock", 15, TimeUnit.SECONDS);
        if (!success) {
            log.debug("ip: {} access repeat", ip);
            return 0;
        }
        //发布消息
        ArticleRabbitMessage message = new ArticleRabbitMessage(ArticleRabbitMessage.Type.VIEW,
                articleId, authorId, loginUserId);
        rabbitMessagePublisher.publishMsg(message);
        return 1;
    }

    @Override
    public ArticleDTO getArticleDTOById(Integer id) {
        String key = Constants.ARTICLE_INFO + id;
        ArticleDTO dto = RedisClient.getCacheObject(key);
        if (dto != null && dto.getId() == null) {
            return null;
        } else if (dto == null) {
            synchronized (this) {
                dto = this.loadArticleDTOFromDB(id);
                if (dto == null) {
                    RedisClient.setCacheObject(key, new ArticleDTO(), 10, TimeUnit.SECONDS);
                } else {
                    RedisClient.setCacheObject(key, dto);
                }
            }
        }
        return dto;
    }

    @Override
    public List<ArticleDTO> listArticleById(Collection<Integer> ids, ArticleOptionsBuilder builder) {
        if (CollectionUtils.isEmpty(ids)) return Collections.emptyList();
        List<ArticleDTO> dtos = this.listArticleDTOById(ids);
        this.buildArticleDTOs(dtos, builder);
        return dtos;
    }

    private List<ArticleDTO> listArticleDTOById(Collection<Integer> ids) {
        return RedisClient.getMultiCacheObject(Constants.ARTICLE_INFO, ids, nullIds -> {
            //fix? 感觉还是单个单个获取比较好
            List<ArticleDTO> results = loadArticleDTOFromDB(nullIds);
            RedisClient.cacheMultiObject(results, a -> Constants.ARTICLE_INFO + a.getId(), 20L, 30L, TimeUnit.DAYS);
            return results;
        });
    }

    @Override
    public boolean saveArticle(Article article) {
        return articleRepository.saveArticle(article);
    }

    @Override
    public boolean updateArticle(Article article) {
        return articleRepository.updateArticle(article);
    }

    @Transactional
    @Override
    public boolean deleteByArticleId(Integer articleId, Integer userId) {
        return articleRepository.deleteArticle(articleId, userId);
    }

    private ArticleDTO loadArticleDTOFromDB(Integer id) {
        ArticleInfo info = articleRepository.getArticleInfoById(id);
        if (info == null) return null;
        ArticleDTO dto = ArticleConverter.INSTANCE.toArticleDTO(info);
        //分类
        CategoryDTO category = categoryService.getCategoryDTOById(info.getCategoryId());
        dto.setCategory(category);

        //文章标签
        List<TagDTO> tags = tagService.getTagDTOsByIds(info.getTagIds());
        dto.setTags(tags);
        return dto;
    }

    private List<ArticleDTO> loadArticleDTOFromDB(List<Integer> nullIds) {
        List<ArticleInfo> infos = articleRepository.listArticleById(nullIds);
        List<ArticleDTO> dtos = ArticleConverter.INSTANCE.toArticleDTOS(infos);
        //分类
        buildCategory(dtos);
        //标签
        buildArticleTags(dtos);
        return dtos;
    }

    private String loadArticleContent(Integer articleId) {
        String key = Constants.ARTICLE_CONTENT + articleId;
        String content = RedisClient.getCacheObject(key);
        if (content == null || content.isEmpty()) {
            //文章内容不会为空
            content = articleRepository.getArticleContentById(articleId);
            RedisClient.setCacheObject(key, content);
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
        //查询作者信息
        builder.ifAuthor(() -> this.buildArticleAuthor(articleDTOS, userIds));
        //查询文章数据
        builder.ifStat(() -> this.buildArticleStat(articleDTOS, articleIds));
        //查询用户行为(点赞，收藏)
        builder.ifInteract(() -> this.buildInteract(articleDTOS, articleIds));
    }

    public void buildCategory(List<ArticleDTO> dtos) {
        dtos.forEach(t -> {
            ArticleInfo info = t.getArticleInfo();
            CategoryDTO categoryDTO = categoryService.getCategoryDTOById(info.getCategoryId());
            t.setCategory(categoryDTO);
        });
    }

    public void buildArticleTags(List<ArticleDTO> dtos) {
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
        boolean liked =
                likeService.hasLiked(loginUserId, articleDTO.getId(), LikeType.ARTICLE);
        boolean collected =
                collectService.hasCollected(loginUserId, articleDTO.getId());
        articleDTO.setLiked(liked);
        articleDTO.setCollected(collected);
    }


    private List<Integer> loadUserArticlesFromDB(Page<Article> page, String redisKey, Integer userId) {
        //全部取出来
        List<Article> articles = articleRepository.listUserArticle(userId);
        return cacheToRedis(page, redisKey, articles);
    }

    private List<Integer> cacheToRedis(Page<Article> page, String key, List<Article> articles) {
        var set = ListMapperHandler.listToZSet(articles, Article::getId, article ->
                (double) article.getCtime().getTime());
        if (!set.isEmpty()) {
            RedisClient.zAddAll(key, set); //no-expire
        }
        int current = (int) page.getCurrent();
        int size = (int) page.getSize();
        page.setTotal(articles.size());
        return ListMapperHandler.subList(articles, Article::getId, current, size);
    }

    private static List<Integer> cacheArticleListToRedis(String key, long current, long size, List<Article> articles) {
        Collection<Tuple> tuples = ListMapperHandler.listToTuple(articles, Article::getId, article -> article.getCtime().getTime());
        RedisClient.zSetAll(key, tuples, RandomUtils.randomLong(20, 30), TimeUnit.DAYS);
        return ListMapperHandler.subList(articles, Article::getId, current, size);
    }
}
