package top.wang3.hami.common.dto;


import lombok.Data;

@Data
public class DataGrowing {

    private Integer userId;
    private String date;
    private Integer articleIncr;
    private Integer viewIncr;
    private Integer articleLikeIncr;
    private Integer commentIncr;
    private Integer collectIncr;
    private Integer followerIncr;
    private Integer cancelFollowIncr;
}
