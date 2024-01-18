package top.wang3.hami.test;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.interact.LikeType;
import top.wang3.hami.common.model.*;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RandomUtils;
import top.wang3.hami.core.mapper.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.profiles.active=test"
        },
        classes = TestApplication.class
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
class HamiCommunityApplicationTest {

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Autowired
    ArticleStatMapper articleStatMapper;

    @Autowired
    ArticleMapper articleMapper;

    @Autowired
    ArticleTagMapper articleTagMapper;

    @Autowired
    ArticleCollectMapper articleCollectMapper;

    @Autowired
    LikeMapper likeMapper;

    @Autowired
    UserFollowMapper userFollowMapper;

    @Autowired
    JdbcClient jdbcClient;

    @Autowired
    UserStatMapper userStatMapper;

    @Autowired
    UserMapper userMapper;

    int MIN_ARTICLE_ID = 1;

    int MAX_ARTICLE_ID = 2000000;

    int MIN_USER_ID = 1;

    int MAX_USER_ID = 100000;

    @Test
    @Order(1)
    void test01() {
        System.out.println(sqlSessionFactory);
        System.out.println(articleMapper);
        System.out.println(articleStatMapper);
    }

    @Test
    @Order(2)
    void generateArticleStat() {
        // 生成article_stat表数据
        log.info("start to gen article-stat");
        int batchSize = 1000;
        int lastId = 0;
        while (true) {
            List<Article> articles = articleMapper.scanArticles(lastId, batchSize);
            if (CollectionUtils.isEmpty(articles)) {
                break;
            }
            lastId = articles.get(articles.size() - 1).getId();
            List<ArticleStat> stats = ListMapperHandler.listTo(
                    articles,
                    a -> {
                        // 随机数据算了, 不太好生成
                        ArticleStat stat = new ArticleStat(a.getId(), a.getUserId());
                        stat.setViews(RandomUtils.randomInt(100));
                        stat.setCollects(RandomUtils.randomInt(10)); // 固定算了
                        stat.setLikes(RandomUtils.randomInt(20)); // 固定算了
                        stat.setComments(RandomUtils.randomInt(30)); // 固定算了
                        return stat;
                    },
                    false
            );
            articleStatMapper.batchInsertArticleStat(stats);
            log.info("insert article-stat success");
        }
        log.info("finish to gen article-stat");
    }

    @Test
    @Order(3)
    void genUserStat() {
        log.info("start to gen user-stat");
        int batchSize = 1000;
        int lastId = 0;
        while (true) {
            List<Integer> userIds = userMapper.scanUserIds(lastId, batchSize);
            if (CollectionUtils.isEmpty(userIds)) {
                break;
            }
            lastId = userIds.get(userIds.size() - 1);
            List<UserStat> stats = articleStatMapper.scanUserStats(userIds);
            for (UserStat stat : stats) {
                // 随机算了
                stat.setTotalFollowings(RandomUtils.randomInt(1, 10));
                stat.setTotalFollowers(RandomUtils.randomInt(1, 10));
            }
            userStatMapper.batchInsertUserStat(stats);
        }
        log.info("finish to gen user-stat");
    }

    @Test
    @Order(4)
    @Disabled
    void updateArticleTag() throws InterruptedException {
        // 生成tagId
        // 一个个生成太麻烦了-_-
        // 跑了31分钟 玩不了一点
        log.info("start to update article-tag");
        ArrayList<List<Article>> articles = new ArrayList<>(1000);
        int id = 1;
        ExecutorService service = Executors.newFixedThreadPool(32);
        CountDownLatch countDownLatch = new CountDownLatch(1000);
        for (int i = 0; i < 1000; i++) {
            ArrayList<Article> list = new ArrayList<>(2000);
            for (int j = 0; j < 2000; j++) {
                Article article = new Article();
                article.setId(id);
                article.setTagIds(getUniqueTag());
                list.add(article);
                id++;
            }
            articles.add(list);
        }
        for (List<Article> list : articles) {
            service.execute(() -> {
                MybatisBatch<Article> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, list);
                MybatisBatch.Method<Article> method = new MybatisBatch.Method<>(ArticleMapper.class);
                mybatisBatch.execute(method.updateById());
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        log.info("finish to update article-tag");
    }

    @Test
    @Order(5)
    @Disabled
    void genArticleCollect() {
        log.info("start to gen article-collect");
        ArrayList<ArticleCollect> items = new ArrayList<>(1600);
        // 每个用户随机收藏1-20篇文章
        for (int i = MIN_USER_ID; i <= MAX_USER_ID; i++) {
            int size = RandomUtils.randomInt(1, 20);
            List<Integer> articleIds = genRandomId(MIN_ARTICLE_ID, MAX_ARTICLE_ID, size);
            final int userId = i;
            List<ArticleCollect> articleCollects = ListMapperHandler.listTo(
                    articleIds,
                    id -> {
                        ArticleCollect item = new ArticleCollect();
                        item.setArticleId(id);
                        item.setUserId(userId);
                        item.setState(Constants.ONE);
                        return item;
                    },
                    false
            );
            items.addAll(articleCollects);
            if (items.size() >= 1500) {
                articleCollectMapper.batchInsertArticleCollect(items);
                items = null;
                items = new ArrayList<>(1600);
            }
        }
        if (!items.isEmpty()) {
            articleCollectMapper.batchInsertArticleCollect(items);
        }
        log.info("finish to gen article-collect");
    }

    @Test
    @Order(6)
    @Disabled
    void genArticleLikeItem() {
        log.info("start to gen article-like-item");
        ArrayList<LikeItem> items = new ArrayList<>(1600);
        // 每个用户随机点赞1-20篇文章
        for (int i = MIN_USER_ID; i <= MAX_USER_ID; i++) {
            int size = RandomUtils.randomInt(1, 20);
            List<Integer> articleIds = genRandomId(MIN_ARTICLE_ID, MAX_ARTICLE_ID, size);
            final int userId = i;
            List<LikeItem> likeItems = ListMapperHandler.listTo(
                    articleIds,
                    id -> {
                        LikeItem item = new LikeItem();
                        item.setLikerId(userId);
                        item.setItemId(id);
                        item.setItemType(LikeType.ARTICLE.getType());
                        item.setState(Constants.ONE);
                        return item;
                    },
                    false
            );
            items.addAll(likeItems);
            if (items.size() >= 1500) {
                likeMapper.batchInsertLikeItem(items);
                items = null;
                items = new ArrayList<>(1600);
            }
        }
        if (!items.isEmpty()) {
            likeMapper.batchInsertLikeItem(items);
        }
        log.info("finish to gen article-like-item");
    }

    @Test
    @Order(7)
    @Disabled
    void genUserFollowItem() {
        log.info("start to gen user-follow-item");
        ArrayList<UserFollow> items = new ArrayList<>(1600);
        // 每个用户随机关注1-10名用户
        for (int i = MIN_USER_ID; i <= MAX_USER_ID; i++) {
            int size = RandomUtils.randomInt(1, 10);
            final int userId = i;
            List<Integer> followings = genRandomId(userId, MIN_USER_ID, MAX_USER_ID, size);
            List<UserFollow> follows = ListMapperHandler.listTo(
                    followings,
                    id -> {
                        UserFollow follow = new UserFollow();
                        follow.setUserId(userId);
                        follow.setFollowing(id);
                        follow.setState(Constants.ONE);
                        return follow;
                    },
                    false
            );
            items.addAll(follows);
            if (items.size() >= 1500) {
                userFollowMapper.batchInsertFollowItem(items);
                items = null;
                items = new ArrayList<>(1600);
            }
        }
        if (!items.isEmpty()) {
            userFollowMapper.batchInsertFollowItem(items);
        }
        log.info("finish to gen user-follow-item");
    }

    private List<Integer> getUniqueTag() {
        return genRandomId(1000, 1060, 3);
    }

    private List<Integer> genRandomId(int start, int end, int size) {
        HashSet<Integer> set = new HashSet<>();
        for (int i = 0; i < size; i++) {
            int id = RandomUtils.randomInt(start, end);
            while (set.contains(id)) {
                id = RandomUtils.randomInt(start, end);
            }
            set.add(id);
        }
        return new ArrayList<>(set);
    }

    private List<Integer> genRandomId(int self, int start, int end, int size) {
        HashSet<Integer> set = new HashSet<>();
        for (int i = 0; i < size; i++) {
            int id = RandomUtils.randomInt(start, end);
            while (id == self || set.contains(id)) {
                id = RandomUtils.randomInt(start, end);
            }
            set.add(id);
        }
        return new ArrayList<>(set);
    }
}