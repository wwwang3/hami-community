package top.wang3.hami.common.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.dto.ArticleDTO;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadingRecordDTO {

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
    private ArticleDTO content;
}