package top.wang3.hami.web;


import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.core.service.article.ArticleStatService;

@SpringBootTest
public class ApplicationTest {

    @Resource
    ArticleStatService articleStatService;

    @Test
    void testUpdate() {
        ArticleStat stat = new ArticleStat();
        stat.setId(1);
        stat.setViews(10);
        articleStatService.updateById(stat);
    }
}
