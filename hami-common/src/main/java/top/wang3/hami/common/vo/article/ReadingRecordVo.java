package top.wang3.hami.common.vo.article;


import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("user_id")
    private Integer userId;

    /**
     * 文章ID
     */
    @JsonProperty("article_id")
    private Integer articleId;

    /**
     * 阅读时间
     */
    @JsonProperty("reading_time")
    private Date readingTime;

    /**
     * 文章内容
     */
    @JsonProperty("content")
    private ArticleVo content;
}
