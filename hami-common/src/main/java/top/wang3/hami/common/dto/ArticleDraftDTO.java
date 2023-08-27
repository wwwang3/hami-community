package top.wang3.hami.common.dto;


import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ArticleDraftDTO {


    /**
     * 主键ID,草稿ID
     */
    private Long id;

    /**
     * 文章ID, 可为空
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
     * 文章内容
     */
    private String content;

    /**
     * 文章标签
     */
    private List<Integer> articleTags;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 草稿状态 0-未发表 1-已发表
     */
    private Byte state;

    /**
     * 版本号
     */
    private Long version;

    /**
     * 创建时间
     */
    private Date ctime;

    /**
     * 更新时间
     */
    private Date mtime;
}
