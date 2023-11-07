package top.wang3.hami.common.dto.interact;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FollowCountItem {

    private Integer userId;
    private Long count;
}
