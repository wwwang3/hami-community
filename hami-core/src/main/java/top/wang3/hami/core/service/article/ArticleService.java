package top.wang3.hami.core.service.article;

import top.wang3.hami.common.dto.ArticleContentDTO;
import top.wang3.hami.common.dto.ArticleDTO;
import top.wang3.hami.common.dto.ArticleInfo;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.request.ArticlePageParam;
import top.wang3.hami.common.dto.request.UserArticleParam;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.core.service.article.impl.ArticleServiceImpl;

import java.util.List;

public interface ArticleService {

    ArticleInfo getArticleInfoById(Integer id);

    PageData<ArticleDTO> listNewestArticles(ArticlePageParam param);

    ArticleContentDTO getArticleContentById(int articleId);

    List<ArticleDTO> getArticleByIds(List<Integer> ids, ArticleServiceImpl.OptionsBuilder builder);

    PageData<ArticleDTO> getUserArticles(UserArticleParam param);

    boolean checkArticleViewLimit(int articleId, int authorId);

    boolean deleteByArticleId(Integer userId, Integer articleId);

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

    boolean saveArticle(Article article);

    boolean updateArticle(Article article);
}
