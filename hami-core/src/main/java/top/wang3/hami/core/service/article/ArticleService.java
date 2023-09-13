package top.wang3.hami.core.service.article;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.dto.ArticleContentDTO;
import top.wang3.hami.common.dto.ArticleDTO;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.request.ArticlePageParam;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.core.service.article.impl.ArticleServiceImpl;

import java.util.List;

public interface ArticleService extends IService<Article> {

    PageData<ArticleDTO> listNewestArticles(ArticlePageParam param);

    ArticleContentDTO getArticleContentById(int articleId);

    boolean checkArticleViewLimit(int articleId, int authorId);

    boolean deleteByArticleId(Integer userId, Integer articleId);

    List<ArticleDTO> getArticleByIds(List<Integer> ids, ArticleServiceImpl.OptionsBuilder builder);

    @FunctionalInterface
    interface Handle {

        void doSomething();
    }

    class OptionsBuilder {
        boolean cate = true;
        boolean tags =  true;
        boolean author = true;
        boolean stat = true;
        boolean interact = true;

        public OptionsBuilder noCate() {
            cate = false;
            return this;
        }

        public OptionsBuilder noTags() {
            tags = false;
            return this;
        }

        public OptionsBuilder noAuthor() {
            author = false;
            return this;
        }

        public OptionsBuilder noStat() {
            stat = false;
            return this;
        }

        public OptionsBuilder noInteract() {
            interact = false;
            return this;
        }

        public void ifCate(Handle handle) {
            if (cate) {
                handle.doSomething();;
            }
        }
        public void ifTags(Handle handle) {
            if (cate) {
                handle.doSomething();;
            }
        }
        public void ifAuthor(Handle handle) {
            if (cate) {
                handle.doSomething();;
            }
        }
        public void ifStat(Handle handle) {
            if (cate) {
                handle.doSomething();;
            }
        }
        public void ifInteract(Handle handle) {
            if (cate) {
                handle.doSomething();;
            }
        }
    }
}
