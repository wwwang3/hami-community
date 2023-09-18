package top.wang3.hami.common.dto.builder;

public class UserOptionsBuilder {
    public boolean stat = true;
    public boolean follow = true;

    public UserOptionsBuilder noStat() {
        stat = false;
        return this;
    }
    public UserOptionsBuilder noFollowState(){
        follow = false;
        return this;
    }

    public static UserOptionsBuilder justInfo() {
        return new UserOptionsBuilder()
                .noStat()
                .noFollowState();
    }

}
