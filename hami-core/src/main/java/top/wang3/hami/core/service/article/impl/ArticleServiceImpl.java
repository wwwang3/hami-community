package top.wang3.hami.core.service.article.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.converter.ArticleConverter;
import top.wang3.hami.common.dto.*;
import top.wang3.hami.common.dto.builder.ArticleOptionsBuilder;
import top.wang3.hami.common.dto.request.ArticlePageParam;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.dto.request.UserArticleParam;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.model.ReadingRecord;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.repository.ArticleRepository;
import top.wang3.hami.core.repository.ArticleTagRepository;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.article.CategoryService;
import top.wang3.hami.core.service.article.TagService;
import top.wang3.hami.core.service.interact.UserInteractService;
import top.wang3.hami.core.service.stat.CountService;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.security.context.IpContext;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleServiceImpl implements ArticleService {

    @Resource
    RabbitTemplate rabbitTemplate;

    private final CategoryService categoryService;
    private final UserService userService;
    private final UserInteractService userInteractService;
    private final CountService countService;
    private final ArticleRepository articleRepository;
    private final ArticleTagRepository articleTagRepository;
    private final TagService tagService;

    private ArticleServiceImpl self;

    @Autowired
    @Lazy
    public void setSelf(ArticleServiceImpl self) {
        this.self = self;
    }

    @Override
    public ArticleInfo getArticleInfoById(Integer id) {
        String key = Constants.ARTICLE_INFO + id;
        ArticleInfo info;
        if (!RedisClient.exist(key)) {
            Article article = articleRepository.getArticleById(id);
            if (article == null) {
                info = null;
                RedisClient.setCacheObject(key, null, 10, TimeUnit.SECONDS);
            } else {
                List<Integer> tagIds = articleTagRepository.getArticleTagIdsById(id);
                info = ArticleConverter.INSTANCE.toArticleInfo(article, tagIds);
                RedisClient.setCacheObject(key, info, 24, TimeUnit.HOURS);
            }
        } else {
            info = RedisClient.getCacheObject(key);
        }
        return info;
    }

    @Override
    public List<ArticleInfo> getArticleInfoByIds(List<Integer> ids) {
        return listArticleByIds(ids);
    }

    @CostLog
    @Override
    public PageData<ArticleDTO> listNewestArticles(ArticlePageParam param) {
        if (param.getPageNum() > 50) return new PageData<>();
        Integer cateId = param.getCateId();
        Page<Article> page = param.toPage();
        //查询文章列表
        List<ArticleInfo> articleInfos = listArticleByCate(page, cateId);
        List<ArticleDTO> articleDTOS = ArticleConverter.INSTANCE.toArticleDTOS(articleInfos);
        buildArticleDTOs(articleDTOS, new ArticleOptionsBuilder());
        return PageData.<ArticleDTO>builder()
                .total(page.getTotal())
                .pageNum(page.getCurrent())
                .data(articleDTOS)
                .build();
    }

    @Override
    public ArticleContentDTO getArticleContentById(int articleId) {
        ArticleInfo article = self.getArticleInfoById(articleId);
        if (article == null || article.getId() == null) {
            return null;
        }
        String content = articleRepository.getArticleContentById(articleId);
        ArticleContentDTO dto = ArticleConverter.INSTANCE.toArticleContentDTO(article, content);
        //文章分类
        ArticleInfo info = dto.getArticleInfo();
        CategoryDTO category = categoryService.getCategoryDTOById(info.getCategoryId());
        dto.setCategory(category);

        //文章标签
        List<TagDTO> tags = tagService.getTagDTOsByIds(info.getTagIds());
        dto.setTags(tags);

        //作者信息
        UserDTO author = userService.getAuthorInfoById(dto.getUserId());
        dto.setAuthor(author);

        //文章数据
        ArticleStatDTO stat = countService.getArticleStatById(articleId);
        dto.setStat(stat);
        if (checkArticleViewLimit(articleId, article.getUserId())) {
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
        //todo fix 还是有并发问题
        if (RedisClient.exist(redisKey)) {
            log.debug("ip: {} access repeat", ip);
            return false;
        } else {
            RedisClient.setCacheObject(redisKey, 1, 15);
            //发布消息
            String exchange = Constants.HAMI_DIRECT_EXCHANGE2;
            rabbitTemplate.convertAndSend(exchange, Constants.ADD_VIEWS_ROUTING, articleId);
            LoginUserContext.getOptLoginUserId()
                    .ifPresent(id -> {
                        rabbitTemplate.convertAndSend(exchange, Constants.READING_RECORD_ROUTING,
                                new ReadingRecord(id, articleId));
                    });
            return true;
        }
    }

    @Override
    public List<ArticleDTO> getArticleByIds(List<Integer> ids, ArticleOptionsBuilder builder) {
        List<ArticleInfo> articles = listArticleByIds(ids);
        List<ArticleDTO> dtos = ArticleConverter.INSTANCE.toArticleDTOS(articles);
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
        List<ArticleInfo> infos = listArticleByIds(ids);
        List<ArticleDTO> dtos = ArticleConverter.INSTANCE.toArticleDTOS(infos);
        buildArticleDTOs(dtos, null);
        return PageData.<ArticleDTO>builder()
                .total(total)
                .data(dtos)
                .pageNum(page.getCurrent())
                .build();
    }

    @Override
    public PageData<ArticleDTO> getFollowUserArticles(PageParam param) {
        //获取关注用户的最新文章
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

    private List<ArticleInfo> listArticleByCate(Page<Article> page, Integer cateId) {
        if (cateId != null && (cateId < 0 || cateId > 10007)) {
            return Collections.emptyList();
        }
        String key = cateId == null ? Constants.ARTICLE_LIST : Constants.CATE_ARTICLE_LIST + cateId;
        List<Integer> ids;
        if (!RedisClient.exist(key)) {
            //count 已经设置过
            ids = loadArticlesFromDB(page, key, cateId);
        } else {
            ids = loadArticlesFromCache(page, key);
        }
        return listArticleByIds(ids);
    }

    @Override
    public boolean saveArticle(Article article) {
        return articleRepository.saveArticle(article);
    }

    @Override
    public boolean updateArticle(Article article) {
        boolean success = articleRepository.updateArticle(article);
        if (success) {
            clearCache(article.getId());
        }
        return success;
    }

    @Transactional
    @Override
    public boolean deleteByArticleId(Integer articleId, Integer userId) {
        boolean success = articleRepository.deleteArticle(articleId, userId);
        if (success) {
            clearCache(articleId);
        }
        return success;
    }

    private void clearCache(Integer id) {
        String key = Constants.ARTICLE_INFO + id;
        RedisClient.deleteObject(key);
    }

    private void buildArticleDTOs(List<ArticleDTO> articleDTOS,
                                  ArticleOptionsBuilder builder) {
        List<Integer> articleIds = ListMapperHandler.listTo(articleDTOS, ArticleDTO::getId);
        List<Integer> userIds = ListMapperHandler.listTo(articleDTOS, ArticleDTO::getUserId);
        if (builder == null) {
            builder = new ArticleOptionsBuilder();
        }
        //查询文章分类
        builder.ifCate(() -> {
            buildCategory(articleDTOS);
        });
        //查询文章标签
        builder.ifTags(() -> {
            buildArticleTags(articleDTOS);
        });
        //查询作者信息
        builder.ifAuthor(() -> {
            buildArticleAuthor(articleDTOS, userIds);
        });
        //查询文章数据
        builder.ifStat(() -> {
            self.buildArticleStat(articleDTOS, articleIds);
        });
        //查询用户行为(点赞，收藏)
        builder.ifInteract(() -> {
            self.buildInteract(articleDTOS, articleIds);
        });
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
        List<Article> articles = articleRepository.listArticlesByCateId(cateId);
        //不出现意外情况这个方法每个分类只会调用一次
        //redis没数据或者Redis g了
        return cacheToRedis(page, key, articles);
    }

    private List<Integer> loadUserArticlesFromDB(Page<Article> page, String redisKey, Integer userId) {
        //全部取出来
        List<Article> articles = articleRepository.queryUserArticles(userId);
        return cacheToRedis(page, redisKey, articles);
    }


    private List<ArticleInfo> listArticleByIds(List<Integer> ids) {
//        return ListMapperHandler.listTo(ids, self::getArticleInfoById);
        List<String> keys = ListMapperHandler.listTo(ids, id -> Constants.ARTICLE_INFO + id);
        return RedisClient.getMultiCacheObject(keys, (key, index) -> {
           return self.getArticleInfoById(ids.get(index));
        });
    }

    private List<Integer> cacheToRedis(Page<Article> page, String key, List<Article> articles) {
        var set = ListMapperHandler.listToZSet(articles, Article::getId, article -> {
            return (double) article.getCtime().getTime();
        });
        if (!set.isEmpty()) {
            RedisClient.zAddAll(key, set); //no-expire
        }
        int current = (int) page.getCurrent();
        int size = (int) page.getSize();
        page.setTotal(articles.size());  //应该没人会在首页翻50页吧^_^
        return ListMapperHandler.subList(articles, Article::getId, current, size);
    }
}
