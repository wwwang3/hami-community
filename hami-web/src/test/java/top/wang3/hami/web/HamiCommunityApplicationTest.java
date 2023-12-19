package top.wang3.hami.web;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import top.wang3.hami.common.model.ArticleCount;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.core.mapper.ArticleMapper;
import top.wang3.hami.core.mapper.ArticleStatMapper;

import java.util.ArrayList;
import java.util.List;


@SpringBootTest
class HamiCommunityApplicationTest {

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Autowired
    ArticleStatMapper articleStatMapper;

    @Autowired
    ArticleMapper articleMapper;

    @Autowired
    JdbcTemplate jdbcTemplate;

    //    @Test
    void test01() {
        MybatisBatch.Method<ArticleStat> method = new MybatisBatch.Method<>(ArticleStatMapper.class);
        ArrayList<ArticleStat> stats = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            ArticleStat stat = new ArticleStat();
            stat.setArticleId(1);
            stat.setLikes(1);
            stats.add(stat);
        }
        new MybatisBatch<>(sqlSessionFactory, stats)
                .execute(method.update(e -> {
                    return new UpdateWrapper<ArticleStat>()
                            .setSql("likes = likes + {0}", e.getLikes())
                            .eq("article_id", e.getArticleId());
                }));
    }

    @Test
    void test02() {
        ArrayList<ArticleStat> stats = new ArrayList<>();
        for (int i = 100000; i < 100002; i++) {
            ArticleStat stat = new ArticleStat();
            stat.setArticleId(i);
            stat.setLikes(100);
            stats.add(stat);
        }
        articleStatMapper.batchUpdateLikes(stats);
    }

    @Test
    void test03() {
        List<ArticleCount> counts =
                articleMapper.selectCateArticleCount();
        System.out.println(counts);
    }
}