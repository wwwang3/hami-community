package top.wang3.hami.security.annotation;


@FunctionalInterface
public interface TFunction<A, B, C> {

    void apply(A a, B b, C c);
}
