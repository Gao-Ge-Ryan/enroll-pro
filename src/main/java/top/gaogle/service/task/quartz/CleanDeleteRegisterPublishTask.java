package top.gaogle.service.task.quartz;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import top.gaogle.common.RegisterConst;
import top.gaogle.dao.master.RegisterPublishMapper;
import top.gaogle.framework.util.CacheUtil;
import top.gaogle.framework.util.DateUtil;
import top.gaogle.framework.util.JsonUtil;
import top.gaogle.pojo.model.RegisterPublishModel;
import top.gaogle.service.MoreTransactionService;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static top.gaogle.common.RegisterConst.ONE_YEAR_MILLIS;

/**
 * 定时任务 清除删除的报名发布信息
 *
 * @author gaogle
 */
@Component("cleanDeleteRegisterPublishTask")
public class CleanDeleteRegisterPublishTask {
    protected final Logger log = LoggerFactory.getLogger(CleanDeleteRegisterPublishTask.class);

    @Value("${spring.mail.username}")
    public String platformMail;


    private final RegisterPublishMapper registerPublishMapper;
    private final MoreTransactionService moreTransactionService;
    private final CacheUtil cacheUtil;

    public CleanDeleteRegisterPublishTask(RegisterPublishMapper registerPublishMapper, MoreTransactionService moreTransactionService, CacheUtil cacheUtil) {
        this.registerPublishMapper = registerPublishMapper;
        this.moreTransactionService = moreTransactionService;
        this.cacheUtil = cacheUtil;
    }

    public void task() {
        String distributedLock = RegisterConst.CLEAN_DELETE_REGISTER_PUBLISH_TASK_KEY;
        boolean execFlag = cacheUtil.distributedLock(distributedLock, distributedLock, 30, TimeUnit.MINUTES);
        if (!execFlag) {
            log.info("{}分布式锁存在跳过执行", distributedLock);
            return;
        }
        try {
            log.info("===定时任务 清除删除的报名发布信息start===");
            List<RegisterPublishModel> registerPublishModels = registerPublishMapper.queryAllDeleteForUpdate();
            Long now = DateUtil.currentTimeMillis();
            if (!CollectionUtils.isEmpty(registerPublishModels)) {
                for (RegisterPublishModel registerPublishModel : registerPublishModels) {
                    String id = registerPublishModel.getId();
                    Boolean delFlag = registerPublishModel.getDelFlag();
                    Long delAt = registerPublishModel.getDelAt();
                    if (StringUtils.isNotEmpty(id) && Boolean.TRUE.equals(delFlag) && delAt != null && (now - delAt >= ONE_YEAR_MILLIS)) {
                        try {
                            moreTransactionService.cleanDeleteRegisterPublish(registerPublishModel);
                        } catch (Exception e) {
                            log.error("clean删除报名发布信息失败：{}", JsonUtil.object2Json(registerPublishModel), e);
                        }
                    }
                }
            }
            log.info("===定时任务 清除删除的报名发布信息end===");
        } catch (Exception e) {
            log.error("定时任务 清除删除的报名发布信息:异常:", e);
        } finally {
            cacheUtil.releaseLock(distributedLock);
        }


    }

}
