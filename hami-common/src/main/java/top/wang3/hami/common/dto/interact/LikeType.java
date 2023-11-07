package top.wang3.hami.common.dto.interact;

import lombok.Getter;
import top.wang3.hami.common.constant.Constants;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum LikeType {

    ARTICLE(Constants.LIKE_TYPE_ARTICLE),
    COMMENT(Constants.LIKE_TYPE_COMMENT);


    final byte type;


    LikeType(byte type) {
        this.type = type;
    }


    public static final Map<Byte, LikeType> LikeTypeMap = new HashMap<>();

    static {
        LikeTypeMap.put(Constants.LIKE_TYPE_ARTICLE, ARTICLE);
        LikeTypeMap.put(Constants.LIKE_TYPE_COMMENT, COMMENT);
    }

    public static LikeType of(Byte type) {
        return LikeTypeMap.get(type);
    }

}
