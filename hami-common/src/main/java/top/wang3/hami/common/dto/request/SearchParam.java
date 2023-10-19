package top.wang3.hami.common.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SearchParam extends PageParam {

    private String keyword;
    private String type;
}
