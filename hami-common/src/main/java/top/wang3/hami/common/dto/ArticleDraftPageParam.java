package top.wang3.hami.common.dto;


import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleDraftPageParam extends PageParam {

    private Integer userId;
    private Byte state;

}
