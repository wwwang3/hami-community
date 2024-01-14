package top.wang3.hami.common.vo.user;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorRank {

    private Integer userId;
    private Double hotIndex;
    private UserVo user;
}
