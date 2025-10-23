package top.gaogle.service.task.quartz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.gaogle.common.RegisterConst;
import top.gaogle.dao.master.OperateLogMapper;
import top.gaogle.framework.util.CacheUtil;
import top.gaogle.framework.util.DateUtil;

import java.util.concurrent.TimeUnit;

/**
 * 定时任务 清理操作日志
 *
 * @author gaogle
 */
@Component("cleanOperateLogTask")
public class CleanOperateLogTask {
    protected final Logger log = LoggerFactory.getLogger(CleanOperateLogTask.class);

    private final CacheUtil cacheUtil;
    private final OperateLogMapper operateLogMapper;

    public CleanOperateLogTask(CacheUtil cacheUtil, OperateLogMapper operateLogMapper) {
        this.cacheUtil = cacheUtil;
        this.operateLogMapper = operateLogMapper;
    }

    public void task() {
        String distributedLock = RegisterConst.CLEAN_OPERATE_LOG_TASK_TASK_KEY;
        boolean execFlag = cacheUtil.distributedLock(distributedLock, distributedLock, 30, TimeUnit.MINUTES);
        if (!execFlag) {
            log.info("{}分布式锁存在跳过执行", distributedLock);
            return;
        }
        try {
            log.info("===定时任务 清理操作日志start===");
            Long oneYearBefore = DateUtil.currentTimeMillis() - RegisterConst.ONE_YEAR_MILLIS;
            operateLogMapper.deleteOneYearBeforeByCreateAt(oneYearBefore);
            log.info("===定时任务 清理操作日志end===");
        } catch (Exception e) {
            log.error("定时任务 清理操作日志:异常:", e);
        } finally {
            cacheUtil.releaseLock(distributedLock);
        }
    }

}
