package top.wang3.hami.common.enums;

import lombok.Getter;
import top.wang3.hami.common.constant.Constants;

@Getter
public enum LikeType {

    ARTICLE(Constants.LIKE_TYPE_ARTICLE),
    COMMENT(Constants.LIKE_TYPE_COMMENT);


    final byte type;


    LikeType(byte type) {
        this.type = type;
    }


}
