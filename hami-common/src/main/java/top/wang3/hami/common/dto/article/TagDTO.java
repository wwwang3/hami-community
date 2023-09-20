package top.wang3.hami.common.dto.article;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagDTO {

    private Integer tagId;
    private String tagName;
}
