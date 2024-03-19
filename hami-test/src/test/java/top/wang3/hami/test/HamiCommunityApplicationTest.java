package top.wang3.hami.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.interact.LikeType;
import top.wang3.hami.common.model.*;
import top.wang3.hami.common.util.DateUtils;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RandomUtils;
import top.wang3.hami.core.mapper.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;


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
    AccountMapper accountMapper;

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
    void genUser() {
        // 生成article_stat表数据
        log.info("start to gen users");
        int batchSize = 1500;
        int lastId = 0;
        while (true) {
            List<Account> accounts = accountMapper.scanAccountAsc(lastId, batchSize);
            if (CollectionUtils.isEmpty(accounts)) {
                break;
            }
            lastId = accounts.get(accounts.size() - 1).getId();
            List<User> users = ListMapperHandler.listTo(accounts, account -> {
                User user = new User();
                user.setUserId(account.getId());
                user.setUsername(account.getUsername());
                return user;
            });
            userMapper.batchInsertUser(users);
            log.info("insert user success");
        }
        log.info("finish to gen users");
    }

    @Test
    @Order(2)
//    @Disabled
    void generateArticleStat() {
        // 生成article_stat表数据
        log.info("start to gen article-stat");
        int batchSize = 1500;
        int lastId = 0;
        while (true) {
            List<Article> articles = articleMapper.scanArticleAsc(lastId, batchSize);
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
                        stat.setCollects(RandomUtils.randomInt(10));
                        stat.setLikes(RandomUtils.randomInt(20));
                        stat.setComments(RandomUtils.randomInt(30));
                        stat.setCtime(a.getCtime());
                        stat.setMtime(a.getMtime());
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
//    @Disabled
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
    @Order(5)
//    @Disabled
    void genArticleCollect() {
        log.info("start to gen article-collect");
        ArrayList<ArticleCollect> items = new ArrayList<>(1600);
        // 每个用户随机收藏1-20篇文章
        Date ctime = new Date(1577808000000L);
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
                        item.setCtime(ctime);
                        item.setMtime(DateUtils.randomDate(2020, 2024));
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
//    @Disabled
    void genArticleLikeItem() {
        log.info("start to gen article-like-item");
        ArrayList<LikeItem> items = new ArrayList<>(1600);
        // 每个用户随机点赞1-20篇文章
        // 2020-01-01
        Date ctime = new Date(1577808000000L);
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
                        item.setCtime(ctime);
                        item.setMtime(DateUtils.randomDate(2020, 2024));
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
//    @Disabled
    void genUserFollowItem() {
        log.info("start to gen user-follow-item");
        ArrayList<UserFollow> items = new ArrayList<>(1600);
        // 每个用户随机关注1-10名用户
        Date ctime = new Date(1577808000000L);
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
                        follow.setCtime(ctime);
                        follow.setMtime(DateUtils.randomDate(2020, 2024));
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