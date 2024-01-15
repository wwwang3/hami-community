package top.wang3.hami.common.vo.article;


import lombok.Data;
import top.wang3.hami.common.dto.article.CategoryDTO;
import top.wang3.hami.common.dto.article.TagDTO;
import top.wang3.hami.common.dto.stat.ArticleStatDTO;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.vo.user.UserVo;

import java.util.List;

@Data
public class ArticleVo {

    private Integer id;

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
    private CategoryDTO category;

    /**
     * 文章标签
     */
    private List<TagDTO> tags;

    /**
     * 文章数据
     */
    private ArticleStatDTO stat;

    private boolean liked = false;
    private boolean collected = false;

}
