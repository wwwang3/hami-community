package top.wang3.hami.common.dto.builder;


public class ArticleOptionsBuilder {
    boolean author = true;
    boolean stat = true;
    boolean interact = true;


    public ArticleOptionsBuilder noAuthor() {
        author = false;
        return this;
    }

    public ArticleOptionsBuilder noStat() {
        stat = false;
        return this;
    }

    public ArticleOptionsBuilder noInteract() {
        interact = false;
        return this;
    }

    public void ifAuthor(Handle handle) {
        if (author) {
            handle.doSomething();
        }
    }

    public void ifStat(Handle handle) {
        if (stat) {
            handle.doSomething();
        }
    }
    public void ifInteract(Handle handle) {
        if (interact) {
            handle.doSomething();
        }
    }

    public static ArticleOptionsBuilder justInfo() {
        return new ArticleOptionsBuilder()
                .noAuthor()
                .noInteract()
                .noStat();
    }

    @FunctionalInterface
    public interface Handle {

        void doSomething();
    }
}
