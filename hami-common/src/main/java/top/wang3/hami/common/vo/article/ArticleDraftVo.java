package top.wang3.hami.common.vo.article;


import lombok.Data;
import top.wang3.hami.common.model.Tag;

import java.util.Date;
import java.util.List;

@Data
public class ArticleDraftVo {


    /**
     * 主键ID,草稿ID
     */
    private Long id;

    /**
     * 文章ID, 没有发表时为空
     */
    private Integer articleId;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 标题
     */
    private String title;

    /**
     * 文章图片地址
     */
    private String picture;

    /**
     * 简介
     */
    private String summary;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 文章标签
     */
    private List<Tag> tags;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 草稿状态 0-未发表 1-已发表
     */
    private Byte state;

    /**
     * 创建时间
     */
    private Date ctime;

    /**
     * 更新时间
     */
    private Date mtime;
}
