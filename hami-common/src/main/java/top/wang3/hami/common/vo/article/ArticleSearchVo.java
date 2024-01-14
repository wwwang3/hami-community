package top.wang3.hami.common.vo.article;

import lombok.Data;
import top.wang3.hami.common.dto.article.CategoryDTO;
import top.wang3.hami.common.vo.user.UserVo;

import java.util.Date;

@Data
public class ArticleSearchVo {
    private Integer id;
    private Integer userId;
    private Integer categoryId;
    private String picture;
    private String title;
    private String summary;
    private Date ctime;

    private UserVo author;
    private CategoryDTO categoryDTO;
}
