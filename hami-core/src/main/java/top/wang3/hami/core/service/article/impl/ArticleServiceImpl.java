package top.wang3.hami.core.service.article.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.converter.ArticleConverter;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.article.*;
import top.wang3.hami.common.dto.builder.ArticleOptionsBuilder;
import top.wang3.hami.common.dto.request.ArticlePageParam;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.dto.request.UserArticleParam;
import top.wang3.hami.common.dto.user.UserDTO;
import top.wang3.hami.common.message.ArticleRabbitMessage;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.repository.ArticleRepository;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.article.CategoryService;
import top.wang3.hami.core.service.article.TagService;
import top.wang3.hami.core.service.common.RabbitMessagePublisher;
import top.wang3.hami.core.service.interact.UserInteractService;
import top.wang3.hami.core.service.stat.CountService;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.security.context.IpContext;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleServiceImpl implements ArticleService {


    private final CategoryService categoryService;
    private final UserService userService;
    private final UserInteractService userInteractService;
    private final CountService countService;
    private final ArticleRepository articleRepository;
    private final TagService tagService;
    private final RabbitMessagePublisher rabbitMessagePublisher;

    @Override
    public ArticleDTO getArticleDTOById(Integer id) {
        String key = Constants.ARTICLE_INFO + id;
        ArticleDTO dto = RedisClient.getCacheObject(key);
        if (dto != null && dto.getId() == null) {
            return null;
        } else if (dto == null) {
            //todo 保证只有一个请求查询数据库写入Redis
            dto = loadArticleDTOFromDB(id);
            if (dto == null) {
                RedisClient.setCacheObject(key, new ArticleDTO(), 10, TimeUnit.SECONDS);
            } else {
                RedisClient.setCacheObject(key, dto, 24, TimeUnit.HOURS);
            }
        }
        return dto;
    }

    @Override
    public List<ArticleDTO> listArticleDTOById(List<Integer> ids) {
        List<String> keys = ListMapperHandler.listTo(ids, id -> Constants.ARTICLE_INFO + id);
        return RedisClient.getMultiCacheObject(keys, (indexes) -> {
            List<Integer> nullIds = ListMapperHandler.listTo(indexes, ids::get);
            return loadArticleDTOFromDB(nullIds);
        });
    }

    @CostLog
    @Override
    public PageData<ArticleDTO> listNewestArticles(ArticlePageParam param) {
        if (param.getPageNum() > 100) return PageData.empty();
        Integer cateId = param.getCateId();
        Page<Article> page = param.toPage();
        //查询文章列表
        List<ArticleDTO> articleDTOS = listArticleByCate(page, cateId);
        buildArticleDTOs(articleDTOS, new ArticleOptionsBuilder());
        return PageData.<ArticleDTO>builder()
                .total(page.getTotal())
                .pageNum(page.getCurrent())
                .data(articleDTOS)
                .build();
    }

    @Override
    public ArticleContentDTO getArticleContentById(int articleId) {
        ArticleDTO article = getArticleDTOById(articleId);
        if (article == null || article.getId() == null) return null;
        String content = articleRepository.getArticleContentById(articleId);
        ArticleContentDTO dto = ArticleConverter.INSTANCE.toArticleContentDTO(article, content);
        //作者信息
        UserDTO author = userService.getAuthorInfoById(dto.getUserId());
        dto.setAuthor(author);
        //文章数据
        ArticleStatDTO stat = countService.getArticleStatById(articleId);
        dto.setStat(stat);
        if (checkArticleViewLimit(articleId, dto.getUserId())) {
            dto.getStat().setViews(stat.getViews() + 1);
            Integer totalViews = dto.getAuthor().getStat().getTotalViews();
            dto.getAuthor().getStat().setTotalViews(totalViews + 1);
        }
        //用户行为
        buildInteract(dto);
        return dto;
    }

    @Override
    public boolean checkArticleViewLimit(int articleId, int authorId) {
        String ip = IpContext.getIp();
        if (ip == null) return false;
        String redisKey = "view:limit:" + ip + ":" + articleId;
        boolean success = RedisClient.setNx(redisKey, "view-lock", 15, TimeUnit.SECONDS);
        if (!success) {
            log.debug("ip: {} access repeat", ip);
            return false;
        }
        //发布消息
        Integer loginUserId = LoginUserContext.getLoginUserIdDefaultNull();
        ArticleRabbitMessage message = new ArticleRabbitMessage(ArticleRabbitMessage.Type.VIEW,
                articleId, authorId, loginUserId);
        rabbitMessagePublisher.publishMsg(message);
        return true;
    }

    @Override
    public List<ArticleDTO> getArticleByIds(List<Integer> ids, ArticleOptionsBuilder builder) {
        List<ArticleDTO> dtos = listArticleDTOById(ids);
        buildArticleDTOs(dtos, builder);
        return dtos;
    }

    @Override
    public PageData<ArticleDTO> getUserArticles(UserArticleParam param) {
        //获取用户文章
        int userId = param.getUserId();
        String redisKey = Constants.LIST_USER_ARTICLE + userId;
        Page<Article> page = param.toPage();
        List<Integer> ids;
        long total;
        if (!RedisClient.exist(redisKey)) {
            ids = loadUserArticlesFromDB(page, redisKey, userId);
            total = page.getTotal();
        } else {
            ids = RedisClient.zRevPage(redisKey, page.getCurrent(), page.getSize());
            total = RedisClient.zCard(redisKey);
        }
        List<ArticleDTO> dtos = listArticleDTOById(ids);
        buildArticleDTOs(dtos, null);
        return PageData.<ArticleDTO>builder()
                .total(total)
                .data(dtos)
                .pageNum(page.getCurrent())
                .build();
    }

    @Override
    public PageData<ArticleDTO> getFollowUserArticles(PageParam param) {
        //获取关注用户的最新文章 //todo
        int loginUserId = 2;
        Page<Article> page = param.toPage();
        List<Integer> articleIds = articleRepository.listFollowUserArticles(page, loginUserId);
        List<ArticleDTO> dtos = this.getArticleByIds(articleIds, null);
        return PageData.<ArticleDTO>builder()
                .pageNum(page.getCurrent())
                .data(dtos)
                .total(page.getTotal())
                .build();
    }

    private List<ArticleDTO> listArticleByCate(Page<Article> page, Integer cateId) {
        String key = cateId == null ? Constants.ARTICLE_LIST : Constants.CATE_ARTICLE_LIST + cateId;
        List<Integer> ids;
        if (!RedisClient.exist(key)) {
            //count 已经设置过
            ids = loadArticlesFromDB(page, key, cateId);
        } else {
            ids = loadArticlesFromCache(page, key);
        }
        return listArticleDTOById(ids);
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
        Map<String, ArticleDTO> map = ListMapperHandler.listToMap(dtos,
                dto -> Constants.ARTICLE_INFO + dto.getId());
        //cache to Redis
        RedisClient.cacheMultiObject(map, 24, TimeUnit.HOURS);
        return dtos;
    }


    private void buildArticleDTOs(List<ArticleDTO> articleDTOS,
                                  ArticleOptionsBuilder builder) {
        List<Integer> articleIds = ListMapperHandler.listTo(articleDTOS, ArticleDTO::getId);
        List<Integer> userIds = ListMapperHandler.listTo(articleDTOS, ArticleDTO::getUserId);
        if (builder == null) {
            builder = new ArticleOptionsBuilder();
        }
        //查询作者信息
        builder.ifAuthor(() -> buildArticleAuthor(articleDTOS, userIds));
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
        List<ArticleStatDTO> stats = countService.getArticleStatByIds(articleIds);
        ListMapperHandler.doAssemble(dtos, ArticleDTO::getId, stats, ArticleStatDTO::getArticleId, ArticleDTO::setStat);
    }

    public void buildArticleAuthor(List<ArticleDTO> dtos, List<Integer> userIds) {
        List<UserDTO> authors = userService.getAuthorInfoByIds(userIds, null);
        ListMapperHandler
                .doAssemble(dtos, ArticleDTO::getUserId, authors, UserDTO::getUserId, ArticleDTO::setAuthor);
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

    private void buildInteract(final ArticleDTO articleDTO) {
        LoginUserContext.getOptLoginUserId()
                .ifPresent(loginUserId -> {
                    boolean liked =
                            userInteractService.hasLiked(loginUserId, articleDTO.getId(), Constants.LIKE_TYPE_ARTICLE);
                    boolean collected =
                            userInteractService.hasCollected(loginUserId, articleDTO.getId(), Constants.LIKE_TYPE_ARTICLE);
                    articleDTO.setLiked(liked);
                    articleDTO.setCollected(collected);
                });
    }

    private List<Integer> loadArticlesFromCache(Page<Article> page, String key) {
        long current = page.getCurrent();
        long size = page.getSize();
        Long count = RedisClient.zCard(key);
        page.setTotal(count);
        return RedisClient.zRevPage(key, current, size);
    }

    private List<Integer> loadArticlesFromDB(Page<Article> page, String key, Integer cateId) {
        List<Article> articles = articleRepository.listArticleByCateId(cateId);
        //不出现意外情况这个方法每个分类只会调用一次
        //redis没数据或者Redis g了
        return cacheToRedis(page, key, articles);
    }

    private List<Integer> loadUserArticlesFromDB(Page<Article> page, String redisKey, Integer userId) {
        //全部取出来
        List<Article> articles = articleRepository.queryUserArticles(userId);
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
        page.setTotal(articles.size());  //应该没人会在首页翻50页吧^_^
        return ListMapperHandler.subList(articles, Article::getId, current, size);
    }
}
