package top.wang3.hami.core.service.interact;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;


@Data
@Accessors(fluent = true, chain = true)
public class Interact<T> {

    private String key;

    private T member;

    private double score;

    private boolean state;


    public static <T> Interact<T> ofDoAction(String key, T member) {
        return ofDoAction(key, member, new Date().getTime());
    }

    public static <T> Interact<T> ofDoAction(String key, T member, double score) {
        return new Interact<T>()
                .state(true)
                .key(key)
                .member(member)
                .score(score);
    }

    public static <T> Interact<T> ofCancelAction(String key, T member) {
        return new Interact<T>()
                .state(false)
                .key(key)
                .member(member)
                .score(-1);
    }

}
