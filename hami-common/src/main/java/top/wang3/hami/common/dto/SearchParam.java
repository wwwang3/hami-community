package top.wang3.hami.common.dto;


import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;


/**
 * 搜索请求参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchParam extends PageParam {

    /**
     * 关键字
     */
    @Length(max = 32, message = "关键字长度为1-32", groups = Article.class)
    private String keyword;

    /**
     * 搜索类型
     * @future 根据类型搜索, 比如用户, 标签等
     */
    private String type;

    public interface Article {

    }
}
