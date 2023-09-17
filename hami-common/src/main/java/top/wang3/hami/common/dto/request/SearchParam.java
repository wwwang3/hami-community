package top.wang3.hami.common.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SearchParam extends PageParam {

    @NotBlank
    private String keyword;
    private String type;
}
