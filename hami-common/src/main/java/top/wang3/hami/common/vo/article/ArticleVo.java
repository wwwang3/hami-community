package top.wang3.hami.common.vo.article;


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
    private Integer id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 文章信息
     */
    private Article articleInfo;

    /**
     * 作者信息
     */
    private UserVo author;

    /**
     * 分类
     */
    private Category category;

    /**
     * 文章标签
     */
    private List<Tag> tags;

    /**
     * 文章数据
     */
    private ArticleStatDTO stat;

    /**
     * 是否点赞
     */
    private boolean liked = false;

    /**
     * 是否收藏
     */
    private boolean collected = false;

}
