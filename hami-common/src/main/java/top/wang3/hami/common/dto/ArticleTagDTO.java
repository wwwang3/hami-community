package top.wang3.hami.common.dto;


import lombok.Data;

import java.util.List;

@Data
public class ArticleTagDTO {

    private int articleId;

    private List<TagDTO> tags;
}
