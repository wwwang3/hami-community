package top.wang3.hami.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.profiles.active=test"
        },
        classes = TestApplication.class
)
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

    int MIN_ARTICLE_ID = 1;

    int MAX_ARTICLE_ID = 2000000;

    int MIN_USER_ID = 1;

    int MAX_USER_ID = 200000;

    @Test
    @Disabled
    void test01() {
        System.out.println(sqlSessionFactory);
        System.out.println(articleMapper);
        System.out.println(articleStatMapper);
    }

    @Test
    @Disabled
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
                    a -> new ArticleStat(a.getId(), a.getUserId()),
                    false
            );
            articleStatMapper.batchInsertArticleStat(stats);
            log.info("insert article-stat success");
        }
        log.info("finish to gen article-stat");
    }

    @Test
    @Disabled
    void genUserStat() {
        log.info("start to gen user-stat");
        List<UserStat> stats = jdbcClient
                .sql("select user_id as userId, count(*) as totalArticles from article group by user_id")
                .query(UserStat.class)
                .stream()
                .toList();
        List<List<UserStat>> lists = ListMapperHandler.split(stats, 1000);
        for (List<UserStat> list : lists) {
            userStatMapper.batchInsertUserStat(list);
        }
        log.info("finish to gen user-stat");
    }

    @Test
    @Disabled
    void generateArticleTag() {
        log.info("start to gen article-tag");
        // 生成article_tag表数据
        int batchSize = 500;
        int lastId = 0;
        while (true) {
            List<Integer> articleIds = articleMapper.scanArticleIds(lastId, batchSize);
            if (CollectionUtils.isEmpty(articleIds)) {
                break;
            }
            lastId = articleIds.get(articleIds.size() - 1);
            ArrayList<ArticleTag> tags = articleIds.stream()
                    .collect(ArrayList::new, (items, id) -> {
                        List<ArticleTag> articleTags = getUniqueTag(id);
                        items.addAll(articleTags);
                    }, ArrayList::addAll);
            articleTagMapper.batchInsertArticleTag(tags);
        }
        log.info("finish to gen article-tag");
    }

    @Test
    @Disabled
    void genArticleCollect() {
        log.info("start to gen article-collect");
        ArrayList<ArticleCollect> items = new ArrayList<>(1600);
        for (int i = MIN_USER_ID; i <= MAX_USER_ID; i++) {
            int size = RandomUtils.randomInt(1, 10);
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
    @Disabled
    void genArticleLikeItem() {
        log.info("start to gen article-like-item");
        ArrayList<LikeItem> items = new ArrayList<>(1600);
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
    @Disabled
    void genUserFollowItem() {
        log.info("start to gen user-follow-item");
        ArrayList<UserFollow> items = new ArrayList<>(1600);
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

    private List<ArticleTag> getUniqueTag(Integer articleId) {
        List<Integer> items = genRandomId(1000, 1060, 3);
        return ListMapperHandler.listTo(
                items,
                tagId -> new ArticleTag(articleId, tagId),
                false
        );
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