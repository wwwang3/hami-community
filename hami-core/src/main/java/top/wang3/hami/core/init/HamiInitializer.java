package top.wang3.hami.core.init;

public interface HamiInitializer extends Runnable {

    InitializerEnums getName();

    void run();

    default boolean alwaysExecute() {
        return false;
    }

    default boolean async() {
        return false;
    }

}
