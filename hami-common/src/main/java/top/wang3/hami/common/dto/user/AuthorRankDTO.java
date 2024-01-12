package top.wang3.hami.common.dto.user;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorRankDTO {

    private Integer userId;
    private Double hotIndex;
    private UserDTO user;
}
