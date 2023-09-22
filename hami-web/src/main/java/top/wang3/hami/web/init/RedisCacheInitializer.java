package top.wang3.hami.web.init;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.repository.ArticleRepository;
import top.wang3.hami.core.repository.UserRepository;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.user.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

//@Component
@RequiredArgsConstructor
public class RedisCacheInitializer implements ApplicationRunner {

    private final ArticleService articleService;
    private final UserService userService;

    private final ArticleRepository articleRepository;

    private final UserRepository userRepository;

    @CostLog
    @Override
    public void run(ApplicationArguments args) throws Exception {
//        List<Integer> ids = articleRepository.listInitArticle();
//        articleService.getArticleByIds(ids, null);
        cacheArticleInfo();
    }


    private void cacheArticleInfo() {
        int page = 1;
        int pageSize = 1000;
        int maxPage = 100000 / 1000;
        for (int i = page; i <= maxPage; i++) {
            ArrayList<Integer> collect = IntStream.range((i - 1) * pageSize + 1, i * pageSize + 1)
                    .collect(ArrayList::new, List::add, ArrayList::addAll);
            System.out.println(collect.get(999));
            System.out.println(collect.size());
            articleService.getArticleByIds(collect, null);
        }
    }
}
