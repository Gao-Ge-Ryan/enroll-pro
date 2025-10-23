package top.gaogle.framework.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * 确保应用退出时能关闭后台线程
 *
 * @author gaogle
 */
@Component
public class ShutdownManager {
    private static final Logger log = LoggerFactory.getLogger(ShutdownManager.class);

    @PreDestroy
    public void destroy() {
        shutdownAsyncManager();
    }

    /**
     * 停止异步执行任务
     */
    private void shutdownAsyncManager() {
        try {
            log.info("===关闭后台任务任务线程池start===");
            AsyncManager.me().shutdown();
            log.info("===关闭后台任务任务线程池end===");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
