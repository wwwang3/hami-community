package top.wang3.hami.common.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class AsyncStopWatch {

    @Getter
    private final String id;

    private final AtomicLong totalNanos = new AtomicLong(0L);
    private final Map<String, TaskInfo> tasks = new HashMap<>();

    public AsyncStopWatch(String id) {
        this.id = id;
    }

    public int getTaskCount() {
        return tasks.size();
    }

    public long getTotalTimeNanos() {
        return totalNanos.get();
    }

    public double getTotalTimeSeconds() {
        return getTotalTime(TimeUnit.SECONDS);
    }

    public double getTotalTime(TimeUnit timeUnit) {
        return (double) this.totalNanos.get() / TimeUnit.NANOSECONDS.convert(1, timeUnit);
    }


    public void start(String id) {
        if (!StringUtils.hasText(id)) {
            throw new IllegalArgumentException("id can not be empty");
        }
        TaskInfo taskInfo = tasks.get(id);
        if (taskInfo != null) {
            throw new IllegalStateException("A task with ID " + id +" already exists.");
        }
        taskInfo = new TaskInfo(id, System.nanoTime());
        tasks.put(id, taskInfo);
    }

    public void stop(String id) {
        TaskInfo task = tasks.get(id);
        if (task == null) {
            throw new IllegalArgumentException("No task with ID " + id + "found");
        }
        task.setEndNanos(System.nanoTime());
        totalNanos.getAndAdd(task.getTimeNanos());
    }

    /**
     * @see StopWatch#prettyPrint()
     */
    public String prettyPrint() {
        return prettyPrint(TimeUnit.SECONDS);
    }

    /**
     * @see org.springframework.util.StopWatch#prettyPrint(TimeUnit)
     * @param timeUnit – the unit to use for rendering total time and task time
     * @return prettyInfo
     */
    public String prettyPrint(TimeUnit timeUnit) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        nf.setMaximumFractionDigits(9);
        nf.setGroupingUsed(false);

        NumberFormat pf = NumberFormat.getPercentInstance(Locale.ENGLISH);
        pf.setMinimumIntegerDigits(2);
        pf.setGroupingUsed(false);

        StringBuilder sb = new StringBuilder(128);
        sb.append("StopWatch '").append(getId()).append("': ");
        String total = (timeUnit == TimeUnit.NANOSECONDS ?
                nf.format(getTotalTimeNanos()) : nf.format(getTotalTime(timeUnit)));
        sb.append(total).append(" ").append(timeUnit.name().toLowerCase(Locale.ENGLISH));
        int width = Math.max(sb.length(), 40);
        sb.append("\n");
        if (!tasks.isEmpty()) {
            String line = "-".repeat(width) + "\n";
            String unitName = timeUnit.name();
            unitName = unitName.charAt(0) + unitName.substring(1).toLowerCase(Locale.ENGLISH);
            unitName = String.format("%-12s", unitName);
            sb.append(line);
            sb.append(unitName).append("  %       Task name\n");
            sb.append(line);

            int digits = total.indexOf('.');
            if (digits < 0) {
                digits = total.length();
            }
            nf.setMinimumIntegerDigits(digits);
            nf.setMaximumFractionDigits(10 - digits);
            for (TaskInfo task : this.tasks.values()) {
                sb.append(String.format("%-14s", (timeUnit == TimeUnit.NANOSECONDS ?
                        nf.format(task.getTimeNanos()) : nf.format(task.getTime(timeUnit)))));
                sb.append(String.format("%-8s",
                        pf.format(task.getTimeSeconds() / getTotalTimeSeconds())));
                sb.append(task.getId()).append('\n');
            }
        } else {
            sb.append("No task info kept");
        }
        return sb.toString();
    }

    public String shortSummary() {
        return "StopWatch '" + getId() + "': " + getTotalTimeSeconds() + " seconds";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(shortSummary());
        if (!this.tasks.isEmpty()) {
            for (TaskInfo task : this.tasks.values()) {
                sb.append("; [").append(task.getId()).append("] took ").append(task.getTimeSeconds()).append(" seconds");
                long percent = Math.round(100.0 * task.getTimeSeconds() / getTotalTimeSeconds());
                sb.append(" = ").append(percent).append('%');
            }
        }
        else {
            sb.append("; no task info kept");
        }
        return sb.toString();
    }

    public static class TaskInfo {

        @Getter
        private final String id;
        private final long startNanos;

        @Setter
        private long endNanos;

        public TaskInfo(String id, long startNanos) {
            this.id = id;
            this.startNanos = startNanos;
        }

        public long getTimeNanos() {
            return endNanos - startNanos;
        }

        public double getTimeSeconds() {
            return getTime(TimeUnit.SECONDS);
        }

        /**
         *
         * @see org.springframework.util.StopWatch.TaskInfo#getTime(TimeUnit)
         */
        public double getTime(TimeUnit timeUnit) {
            return (double) this.getTimeNanos() / TimeUnit.NANOSECONDS.convert(1, timeUnit);
        }

    }
}
