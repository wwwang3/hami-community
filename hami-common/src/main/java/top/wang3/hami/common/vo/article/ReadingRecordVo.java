package top.wang3.hami.common.vo.article;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 阅读记录
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadingRecordVo {

    /**
     * ID
     */
    private Integer id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 文章ID
     */
    private Integer articleId;

    /**
     * 阅读时间
     */
    private Date readingTime;

    /**
     * 文章内容
     */
    private ArticleVo content;
}
