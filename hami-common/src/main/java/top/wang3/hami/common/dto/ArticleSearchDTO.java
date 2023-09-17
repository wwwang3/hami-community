package top.wang3.hami.common.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ArticleSearchDTO {
    private Integer id;
    private Integer userId;
    private Integer categoryId;
    private String picture;
    private String title;
    private String summary;
    private Date ctime;

    private UserDTO author;
    private CategoryDTO categoryDTO;
}
