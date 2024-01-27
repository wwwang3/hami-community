package top.wang3.hami.common.dto;


import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 搜索请求参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchParam extends PageParam {

    /**
     * 关键字
     */
    @Pattern(regexp = "^\\d{2,32}$", message = "关键字长度为2-32")
    private String keyword;

    /**
     * 搜索类型
     * @future 根据类型搜索, 比如用户, 标签等
     */
    private String type;

}
