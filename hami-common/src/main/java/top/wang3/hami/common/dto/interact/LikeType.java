package top.wang3.hami.common.dto.interact;

import lombok.Getter;
import top.wang3.hami.common.constant.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 点赞类型
 */
@Getter
public enum LikeType {

    /**
     * 文章点赞
     */
    ARTICLE(Constants.LIKE_TYPE_ARTICLE),

    /**
     * 评论点赞
     */
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
