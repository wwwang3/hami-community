package top.wang3.hami.common.dto.notify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Info {
    private Integer id;
    private String name;
    private String image;
    private String detail;
}