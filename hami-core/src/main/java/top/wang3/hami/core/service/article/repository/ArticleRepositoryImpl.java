package top.wang3.hami.core.service.article.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import top.wang3.hami.common.converter.ArticleConverter;
import top.wang3.hami.common.dto.article.ArticleInfo;
import top.wang3.hami.common.dto.article.ArticleSearchDTO;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.model.ArticleDO;
import top.wang3.hami.common.model.ArticleTag;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.component.ZPageHandler;
import top.wang3.hami.core.mapper.ArticleMapper;

import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ArticleRepositoryImpl extends ServiceImpl<ArticleMapper, Article>
        implements ArticleRepository {

    private final String[] fields = {"id", "user_id", "title", "summary", "picture", "category_id", "ctime", "mtime"};
    private final String[] full_fields = {"id", "user_id", "title", "summary", "content", "picture", "category_id", "ctime", "mtime"};


    @Override
    public ArticleInfo getArticleInfoById(Integer articleId) {
        ArticleDO articleDO = getBaseMapper().selectArticleById(articleId);
        if (articleDO == null) return null;
        List<Integer> tagIds = ListMapperHandler.listTo(articleDO.getTags(), ArticleTag::getTagId);
        return ArticleConverter.INSTANCE.toArticleInfo(articleDO, tagIds);
    }

    @Override
    public Long getArticleCount(Integer cateId, Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("id")
                .eq(userId != null, "user_id", userId)
                .eq(cateId != null, "category_id", cateId)
                .count();
    }

    @Override
    public List<ArticleInfo> listArticleById(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) return Collections.emptyList();
        List<ArticleDO> dos = getBaseMapper().listArticleById(ids);
        return ArticleConverter.INSTANCE.toArticleInfos(dos);
    }

    @Override
    public List<Article> listUserArticle(int userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("id", "ctime")
                .eq("user_id", userId)
                .orderByDesc("ctime")
                .last("limit " + ZPageHandler.DEFAULT_MAX_SIZE)
                .list();
    }

    @Override
    public List<Article> listArticleByCateId(Integer cateId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("id", "user_id", "ctime")
                .eq(cateId != null, "category_id", cateId)
                .orderByDesc("ctime") //根据ctime倒序
                .last("limit " + ZPageHandler.DEFAULT_MAX_SIZE)
                .list();
    }

    @Override
    public List<Integer> listArticleByPage(Page<Article> page, Integer cateId, Integer userId) {
        List<Article> articles = ChainWrappers.queryChain(getBaseMapper())
                .select("id")
                .eq(userId != null, "user_id", userId)
                .eq("category_id", cateId)
                .orderByDesc("ctime")
                .list(page);
        return ListMapperHandler.listTo(articles, Article::getId);
    }

    @Override
    public String getArticleContentById(Integer articleId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("content")
                .eq("id", articleId)
                .oneOpt()
                .map(Article::getContent)
                .orElse("");
    }

    @Override
    public List<Integer> scanArticleIds(int lastId, int batchSize) {
        return getBaseMapper().scanArticleIds(lastId, batchSize);
    }

    @Override
    public List<ArticleDO> scanArticles(List<Integer> ids) {
       return getBaseMapper().scanArticles(ids);
    }

    @Override
    public boolean checkArticleExist(Integer articleId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("id")
                .eq("id", articleId)
                .exists();
    }


    @Override
    public List<ArticleSearchDTO> searchArticle(Page<Article> page, String keyword) {
        if (!StringUtils.hasText(keyword)) return Collections.emptyList();
        return getBaseMapper().searchArticle(page, keyword);
    }

    @Override
    public List<Integer> listFollowUserArticles(Page<Article> page, int loginUserId) {
        return getBaseMapper().selectFollowUserArticles(page, loginUserId);
    }

    @Override
    public List<Integer> listInitArticle() {
        List<Article> articles = ChainWrappers.queryChain(getBaseMapper())
                .select("id")
                .orderByDesc("id")
                .last("limit 2000")
                .list();
        return ListMapperHandler.listTo(articles, Article::getId);
    }

    @Override
    public Integer getArticleAuthor(Integer articleId) {
        return getBaseMapper().getArticleAuthor(articleId);
    }

    @Transactional
    @Override
    public boolean saveArticle(Article article) {
        Assert.notNull(article, "article can not be null");
        return super.save(article);
    }

    @Transactional
    @Override
    public boolean updateArticle(Article article) {
        Assert.notNull(article, "article can not be null");
        return super.updateById(article);
    }

    @Transactional
    @Override
    public boolean deleteArticle(Integer articleId, Integer userId) {
        return ChainWrappers.updateChain(getBaseMapper())
                .eq("user_id", userId)
                .eq("id", articleId)
                .remove();
    }
}
