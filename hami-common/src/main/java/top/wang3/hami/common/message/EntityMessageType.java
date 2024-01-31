package top.wang3.hami.common.message;

import lombok.Getter;

@Getter
public enum EntityMessageType {

    CREATE(".create"),
    UPDATE(".update"),
    DELETE(".delete");

    final String suffix;

    EntityMessageType(String suffix) {
        this.suffix = suffix;
    }
}
