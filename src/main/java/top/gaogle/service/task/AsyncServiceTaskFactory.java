package top.gaogle.service.task;


import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.gaogle.common.RegisterConst;
import top.gaogle.dao.master.ActivityInfoMapper;
import top.gaogle.dao.master.RegisterPublishMapper;
import top.gaogle.framework.util.CacheUtil;
import top.gaogle.framework.util.DateUtil;
import top.gaogle.pojo.dto.RegisterPublishStatusUpdateDTO;
import top.gaogle.pojo.enums.RegisterPublishStatusEnum;
import top.gaogle.pojo.model.ActivityInfoModel;
import top.gaogle.pojo.model.RegisterPublishModel;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * 异步工厂（产生业务任务用）
 *
 * @author gaogle
 */
@Component
public class AsyncServiceTaskFactory {

    private static final Logger log = LoggerFactory.getLogger(AsyncServiceTaskFactory.class);

    private final RegisterPublishMapper registerPublishMapper;
    private final CacheUtil cacheUtil;
    private final ActivityInfoMapper activityInfoMapper;

    @Autowired
    public AsyncServiceTaskFactory(RegisterPublishMapper registerPublishMapper, CacheUtil cacheUtil, ActivityInfoMapper activityInfoMapper) {
        this.registerPublishMapper = registerPublishMapper;
        this.cacheUtil = cacheUtil;
        this.activityInfoMapper = activityInfoMapper;
    }

    /**
     * 更新报名发布状态
     */
    public TimerTask updateRegisterPublishStatusTask() {
        return new TimerTask() {
            @Override
            public void run() {
                String distributedLock = RegisterConst.UPDATE_REGISTER_PUBLISH_STATUS_TASK_KEY;
                boolean execFlag = cacheUtil.distributedLock(distributedLock, distributedLock, 30, TimeUnit.MINUTES);
                if (!execFlag) {
                    log.info("{}分布式锁存在跳过执行", distributedLock);
                    return;
                }
                try {
                    log.info("===更新报名发布状态定时任务start===");
                    List<RegisterPublishModel> registerPublishModels = registerPublishMapper.queryAllExcludeStatus(RegisterPublishStatusEnum.ARCHIVE);
                    if (CollectionUtils.isNotEmpty(registerPublishModels)) {
                        Long timeMillis = DateUtil.currentTimeMillis();
                        List<RegisterPublishStatusUpdateDTO> updates = new ArrayList<>();
                        for (RegisterPublishModel model : registerPublishModels) {
                            String id = model.getId();
                            try {
                                RegisterPublishStatusEnum status = determineStatus(timeMillis, model);
                                if (status != null) {
                                    updates.add(new RegisterPublishStatusUpdateDTO(id, status));
                                }
                            } catch (Exception e) {
                                log.error("定时任务更新报名发布状态registerPublishId:{}异常", id, e);
                            }
                        }
                        // 批量更新数据库
                        if (CollectionUtils.isNotEmpty(updates)) {
                            registerPublishMapper.batchUpdateStatus(updates);
                        }
                    }
                    log.info("===更新报名发布状态定时任务end===");
                } catch (Exception e) {
                    log.error("定时任务:更新报名发布状态:异常:", e);
                } finally {
                    cacheUtil.releaseLock(distributedLock);
                }
            }
        };
    }

    /**
     * 根据当前时间和状态模型确定状态
     */
    private RegisterPublishStatusEnum determineStatus(Long timeMillis, RegisterPublishModel model) {
        String id = model.getId();
        Long startAt = model.getStartAt();
        Long endAt = model.getEndAt();

        Boolean ticketFlag = model.getTicketFlag();
        Long ticketStartAt = model.getTicketStartAt();
        Long ticketEndAt = model.getTicketEndAt();

        Boolean activityFlag = model.getActivityFlag();
//        Long activityStartAt = model.getActivityStartAt();
//        Long activityEndAt = model.getActivityEndAt();

        Boolean scoreFlag = model.getScoreFlag();
        Long scoreStartAt = model.getScoreStartAt();
        Long scoreEndAt = model.getScoreEndAt();

        Boolean interviewTicketFlag = model.getInterviewTicketFlag();
        Long interviewTicketStartAt = model.getInterviewTicketStartAt();
        Long interviewTicketEndAt = model.getInterviewTicketEndAt();

        Boolean interviewScoreFlag = model.getInterviewScoreFlag();
        Long interviewScoreStartAt = model.getInterviewScoreStartAt();
        Long interviewScoreEndAt = model.getInterviewScoreEndAt();

        if (timeMillis < startAt) return RegisterPublishStatusEnum.WAITING_TO_REGISTER;
        if (timeMillis <= endAt) return RegisterPublishStatusEnum.REGISTRATION_ONGOING;

        if (Boolean.TRUE.equals(ticketFlag) && timeMillis < ticketStartAt)
            return RegisterPublishStatusEnum.AWAITING_ID_PRINTING;
        if (Boolean.TRUE.equals(ticketFlag) && timeMillis <= ticketEndAt)
            return RegisterPublishStatusEnum.PRINT_EXAM_ADMISSION_TICKET;

        if (Boolean.TRUE.equals(activityFlag)) {
            List<ActivityInfoModel> activityInfoModels = activityInfoMapper.queryByRegisterPublishId(id);
            if (CollectionUtils.isNotEmpty(activityInfoModels)) {
                ActivityInfoModel first = activityInfoModels.get(0);
                ActivityInfoModel last = activityInfoModels.get(activityInfoModels.size() - 1);
                Long activityStartAt = first.getActivityStartAt();
                Long activityEndAt = last.getActivityEndAt();
                if (Boolean.TRUE.equals(activityFlag) && timeMillis < activityStartAt)
                    return RegisterPublishStatusEnum.WAITING_FOR_THE_EXAM;
                if (Boolean.TRUE.equals(activityFlag) && timeMillis <= activityEndAt)
                    return RegisterPublishStatusEnum.THE_EXAM_IS_IN_PROGRESS;
            }
        }

        if (Boolean.TRUE.equals(scoreFlag) && timeMillis < scoreStartAt)
            return RegisterPublishStatusEnum.WAITING_FOR_RESULT_INQUIRY;
        if (Boolean.TRUE.equals(scoreFlag) && timeMillis <= scoreEndAt) return RegisterPublishStatusEnum.SCORE_INQUIRY;

        if (Boolean.TRUE.equals(interviewTicketFlag) && timeMillis < interviewTicketStartAt)
            return RegisterPublishStatusEnum.WAITING_FOR_INTERVIEW_TICKET;
        if (Boolean.TRUE.equals(interviewTicketFlag) && timeMillis <= interviewTicketEndAt)
            return RegisterPublishStatusEnum.PRINT_FOR_INTERVIEW_TICKET;
        if (Boolean.TRUE.equals(interviewScoreFlag) && timeMillis < interviewScoreStartAt)
            return RegisterPublishStatusEnum.WAITING_FOR_INTERVIEW_SCORE;
        if (Boolean.TRUE.equals(interviewScoreFlag) && timeMillis <= interviewScoreEndAt)
            return RegisterPublishStatusEnum.INTERVIEW_SCORE_INQUIRY;
        return RegisterPublishStatusEnum.COMPLETED;
    }


}
