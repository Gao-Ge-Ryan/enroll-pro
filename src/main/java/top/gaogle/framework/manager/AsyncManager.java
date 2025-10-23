package top.gaogle.framework.manager;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import top.gaogle.framework.util.SpringUtil;
import top.gaogle.framework.util.Threads;

import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 异步任务管理器
 *
 * @author gaogle
 */
public class AsyncManager {
    /**
     * 操作延迟10毫秒
     */
    private final int OPERATE_DELAY_TIME = 10;

    /**
     * 异步操作任务调度线程池
     */
    private final ScheduledExecutorService scheduledExecutor = SpringUtil.getBean("scheduledExecutorService");
    private final ThreadPoolTaskExecutor taskExecutor = SpringUtil.getBean("threadPoolTaskExecutor");

    /**
     * 单例模式
     */
    private AsyncManager() {
    }

    private static final AsyncManager me = new AsyncManager();

    public static AsyncManager me() {
        return me;
    }

    /**
     * 执行任务
     *
     * @param task 任务
     */
    public void execute(TimerTask task) {
        scheduledExecutor.schedule(task, OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * 执行任务
     *
     * @param task 任务
     */
    public void scheduleWithFixedDelay(TimerTask task, long initialDelay, long delay, TimeUnit unit) {
        scheduledExecutor.scheduleWithFixedDelay(task, initialDelay, delay, unit);
    }

    /**
     * 停止任务线程池
     */
    public void shutdown() {
        Threads.shutdownAndAwaitTermination(scheduledExecutor);
        Threads.shutdownAndAwaitTermination(taskExecutor.getThreadPoolExecutor());
    }
}
