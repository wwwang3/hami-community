package top.wang3.hami.common.dto;


import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ArticleDTO {

    private int id;

    private int userId;

    private int categoryId;

    private String title;

    private String summary;

    private String picture;

    private Date ctime;

    private Date mtime;

    private CategoryDTO category;

    private List<TagDTO> tags;

    private UserDTO author;

    private ArticleStatDTO stat;

    /**
     * 是否点赞文章
     */
    private boolean liked;

    /**
     * 是否
     */
    private boolean collected;

}
