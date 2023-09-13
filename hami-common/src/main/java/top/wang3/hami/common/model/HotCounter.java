package top.wang3.hami.common.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotCounter {

    private Integer articleId;
    private Double hotRank;
}
