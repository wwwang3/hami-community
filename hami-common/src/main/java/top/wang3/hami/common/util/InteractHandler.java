package top.wang3.hami.common.util;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;
import top.wang3.hami.common.HamiFactory;
import top.wang3.hami.common.lock.LockTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class InteractHandler {

    public static long DEFAULT_EXPIRE = TimeUnit.DAYS.toMillis(1);

    public static <T> AbstractInteract<T> build(String  opt) {
        return new CommonInteract<>(opt);
    }

    public static abstract class AbstractInteract<T> {
        private String key;
        private final String opt;
        private T member;
        private double score;
        private boolean state;
        private long mills = DEFAULT_EXPIRE;

        /**
         * 预检查, 在这判断member是否合法或者该member的请求为重复行为
         */
        private final List<Consumer<T>> preChecks = new ArrayList<>(2);

        /**
         * 加载缓存
         */
        private Runnable loader;

        /**
         * 操作成功后的后置处理
         */
        private final List<Act> postActs = new ArrayList<>(2);

        public AbstractInteract(String opt) {
            this.opt = StringUtils.hasText(opt) ? opt : "操作";
        }

        public AbstractInteract<T> of(String key, T member, boolean state) {
            return state ? ofAction(key, member) : ofCancelAction(key, member);
        }

        public AbstractInteract<T> ofAction(String key, T member) {
           return ofAction(key, member, System.currentTimeMillis());
        }

        public AbstractInteract<T> ofAction(String key, T member, double score) {
            this.key = key;
            this.member = member;
            this.score = score;
            this.state = true;
            return this;
        }

        public AbstractInteract<T> ofCancelAction(String key, T member) {
            this.key = key;
            this.member = member;
            this.state = false;
            return this;
        }

        public AbstractInteract<T> preCheck(Consumer<T> preCheck) {
            if (preCheck != null) {
                preChecks.add(preCheck);
            }
            return this;
        }

        public AbstractInteract<T> postAct(Runnable postAct) {
            return postAct(postAct, false);
        }

        public AbstractInteract<T> postAct(Runnable postAct, boolean sync) {
            if (postAct != null) {
                postActs.add(new Act(postAct, sync));
            }
            return this;
        }

        public AbstractInteract<T> millis(long mills) {
            this.mills = Math.max(mills, DEFAULT_EXPIRE);
            return this;
        }

        public AbstractInteract<T> loader(Runnable loader) {
            this.loader = loader;
            return this;
        }

        protected void doPreCheck() {
            for (Consumer<T> preCheck : preChecks) {
                preCheck.accept(member);
            }
        }

        protected void expiredThenExecuteLoader() {
            boolean success = RedisClient.pExpire(key, mills);
            if (!success) {
                LockTemplate lockTemplate = HamiFactory.getLockTemplate();
                lockTemplate.execute(key, () -> {
                    if (!RedisClient.pExpire(key, mills)) {
                        // 加锁后在判断一次是否有缓存, 没有则执行
                        loader.run();
                    }
                });
            }
        }

        protected void doPostAct() {
            ThreadPoolTaskExecutor executor = HamiFactory.getTaskExecutor();
            for (Act postAct : postActs) {
                if (postAct.sync) {
                    postAct.run();
                } else {
                    executor.execute(postAct);
                }
            }
        }

        public boolean execute() {
            // pre-check
            doPreCheck();
            // 检查缓存是否失效
            expiredThenExecuteLoader();
            // 到这里不存在缓存过期或者zset压根没有数据
            boolean success = executeAction(state);
            if (!success) return false;
            // post-act
            doPostAct();
            // success
            return true;
        }

        private boolean executeAction(boolean state) {
            if (state) return handleAction(opt, key, member, score);
            return handleCancelAction(opt, key, member);
        }

        protected abstract boolean handleAction(String opt, String key, T member, double score);

        protected abstract boolean handleCancelAction(String opt, String key, T member);

    }

    private static class Act implements Runnable {
        boolean sync;

        Runnable postAct;

        public Act(Runnable postAct, boolean sync) {
            this.sync = sync;
            this.postAct = postAct;
        }

        @Override
        public void run() {
            postAct.run();
        }
    }


    private static class CommonInteract<T> extends AbstractInteract<T> {

        public CommonInteract(String opt) {
            super(opt);
        }

        @Override
        protected boolean handleAction(String opt, String key, T member, double score) {
            boolean success = RedisClient.zAddIfAbsent(key, member, score);
            if (!success) {
                // 重复操作
                throw new IllegalArgumentException("重复" + opt);
            }
            return true;
        }

        @Override
        protected boolean handleCancelAction(String opt, String key, T member) {
            Long result = RedisClient.zRem(key, member);
            if (!Objects.equals(1L, result)) {
                // 取消操作失败
                throw new IllegalStateException("取消"+ opt + "失败");
            }
            return true;
        }

    }

}
