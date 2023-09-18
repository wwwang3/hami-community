package top.wang3.hami.common.dto.builder;


public class ArticleOptionsBuilder {
    boolean cate = true;
    boolean tags =  true;
    boolean author = true;
    boolean stat = true;
    boolean interact = true;

    public ArticleOptionsBuilder noCate() {
        cate = false;
        return this;
    }

    public ArticleOptionsBuilder noTags() {
        tags = false;
        return this;
    }

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

    public void ifCate(Handle handle) {
        if (cate) {
            handle.doSomething();;
        }
    }
    public void ifTags(Handle handle) {
        if (cate) {
            handle.doSomething();;
        }
    }
    public void ifAuthor(Handle handle) {
        if (cate) {
            handle.doSomething();;
        }
    }

    public void ifStat(Handle handle) {
        if (cate) {
            handle.doSomething();;
        }
    }
    public void ifInteract(Handle handle) {
        if (cate) {
            handle.doSomething();;
        }
    }

    public static ArticleOptionsBuilder justInfo() {
        return new ArticleOptionsBuilder()
                .noAuthor()
                .noCate()
                .noInteract()
                .noStat()
                .noTags();
    }

    @FunctionalInterface
    public interface Handle {

        void doSomething();
    }
}
