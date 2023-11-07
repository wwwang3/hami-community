package top.wang3.hami.common.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SearchParam extends PageParam {

    @Length(max = 32, message = "关键字长度为1-32", groups = Article.class)
    private String keyword;
    private String type;

    public interface Article {

    }
}
