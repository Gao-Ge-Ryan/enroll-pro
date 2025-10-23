package top.gaogle.service.task;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.gaogle.dao.master.SysJobMapper;
import top.gaogle.framework.manager.AsyncFactory;
import top.gaogle.framework.manager.AsyncManager;
import top.gaogle.framework.util.ScheduleUtils;
import top.gaogle.pojo.domain.SysJob;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class InitTask {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Scheduler scheduler;
    private final SysJobMapper jobMapper;
    private final AsyncServiceTaskFactory asyncServiceTaskFactory;


    @Autowired
    public InitTask(Scheduler scheduler, SysJobMapper jobMapper, AsyncServiceTaskFactory asyncServiceTaskFactory) {
        this.scheduler = scheduler;
        this.jobMapper = jobMapper;
        this.asyncServiceTaskFactory = asyncServiceTaskFactory;
    }


    @PostConstruct
    public void initTokenClean() {
        AsyncManager.me().scheduleWithFixedDelay(AsyncFactory.tokenClean(), 10, 60, TimeUnit.MINUTES);
        log.info("{initTokenClean} Initialization complete");
    }

    @PostConstruct
    public void initRegisterPublishStatusTask() {
        AsyncManager.me().scheduleWithFixedDelay(
                asyncServiceTaskFactory.updateRegisterPublishStatusTask(), 60, 60, TimeUnit.SECONDS);
        log.info("{initTokenClean} Initialization complete");
    }

    /**
     * 项目启动时，初始化定时器 主要是防止手动修改数据库导致未同步到定时任务处理（注：不能手动修改数据库ID和任务组名，否则会导致脏数据）
     */
    @PostConstruct
    public void initQuartzJobs() throws SchedulerException {
        scheduler.clear();
        List<SysJob> jobList = jobMapper.selectJobAll();
        for (SysJob job : jobList) {
            ScheduleUtils.createScheduleJob(scheduler, job);
        }
        log.info("{initQuartzJobs} Initialization complete");
    }

}
