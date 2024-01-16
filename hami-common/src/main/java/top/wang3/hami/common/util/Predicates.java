package top.wang3.hami.common.util;


import java.util.function.Consumer;
import java.util.function.Supplier;

public class Predicates {

    public static Checker check(boolean origin) {
        return new Checker(origin);
    }

    /**
     * 后置判断依赖前一个判断, 前一个判断为true才执行后面的判断
     */
    public static class Checker {

        protected boolean current;

        public Checker() {
        }

        public Checker(boolean origin) {
            this.current = origin;
        }

        public Checker and(boolean other) {
            current = (current && other);
            return this;
        }

        public Checker or(boolean other) {
            current = (current || other);
            return this;
        }

        public Checker then(Supplier<Boolean> step) {
            if (current) {
                Boolean other = step.get();
                current = (other != null && other);
            }
            return this;
        }

        public boolean get() {
            return current;
        }

        public void end(Consumer<Boolean> consumer) {
            consumer.accept(current);
        }

        public Checker ifTrue(Runnable runnable) {
            if (current) {
                runnable.run();
            }
            return this;
        }

        public Checker ifFalse(Runnable runnable) {
            if (!current) {
                runnable.run();
            }
            return this;
        }

    }
}
