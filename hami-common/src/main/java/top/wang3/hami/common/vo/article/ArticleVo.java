package top.wang3.hami.common.vo.article;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import top.wang3.hami.common.dto.stat.ArticleStatDTO;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.model.Category;
import top.wang3.hami.common.model.Tag;
import top.wang3.hami.common.vo.user.UserVo;

import java.util.List;

/**
 * 文章
 */
@Data
public class ArticleVo {

    /**
     * 文章ID
     */
    @JsonProperty("id")
    private Integer id;

    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    private Integer userId;

    /**
     * 文章信息
     */
    @JsonProperty("article_info")
    private Article articleInfo;

    /**
     * 作者信息
     */
    @JsonProperty("author")
    private UserVo author;

    /**
     * 分类
     */
    @JsonProperty("category")
    private Category category;

    /**
     * 文章标签
     */
    @JsonProperty("tags")
    private List<Tag> tags;

    /**
     * 文章数据
     */
    @JsonProperty("stat")
    private ArticleStatDTO stat;

    /**
     * 是否点赞
     */
    @JsonProperty("liked")
    private boolean liked = false;

    /**
     * 是否收藏
     */
    @JsonProperty("collected")
    private boolean collected = false;

}
