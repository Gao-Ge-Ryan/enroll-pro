package top.gaogle.service;

import com.github.pagehelper.page.PageMethod;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.gaogle.dao.master.RegisterPublishAnnouncementMapper;
import top.gaogle.dao.master.RegisterPublishMapper;
import top.gaogle.framework.i18n.I18ResultCode;
import top.gaogle.framework.i18n.I18nResult;
import top.gaogle.framework.pojo.PageModel;
import top.gaogle.framework.util.CronUtils;
import top.gaogle.framework.util.DateUtil;
import top.gaogle.framework.util.SecurityUtil;
import top.gaogle.framework.util.UniqueUtil;
import top.gaogle.pojo.domain.SysJob;
import top.gaogle.pojo.enums.HttpStatusEnum;
import top.gaogle.pojo.enums.RegisterPublishAnnouncementTypeEnum;
import top.gaogle.pojo.model.RegisterPublishAnnouncementModel;
import top.gaogle.pojo.model.RegisterPublishModel;
import top.gaogle.pojo.param.RegisterPublishAnnouncementEditParam;
import top.gaogle.pojo.param.RegisterPublishAnnouncementQueryParam;

import java.util.List;

import static top.gaogle.common.RegisterConst.*;

@Service
public class RegisterPublishAnnouncementService extends SuperService {
    private final RegisterPublishAnnouncementMapper registerPublishAnnouncementMapper;
    private final RegisterPublishMapper registerPublishMapper;
    private final SysJobService sysJobService;

    @Autowired
    public RegisterPublishAnnouncementService(RegisterPublishAnnouncementMapper registerPublishAnnouncementMapper, RegisterPublishMapper registerPublishMapper, SysJobService sysJobService) {
        this.registerPublishAnnouncementMapper = registerPublishAnnouncementMapper;
        this.registerPublishMapper = registerPublishMapper;
        this.sysJobService = sysJobService;
    }

    public I18nResult<Boolean> add(RegisterPublishAnnouncementEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String loginUsername = SecurityUtil.getLoginUsername();
            String enterpriseId = SecurityUtil.getEnterpriseId();
            String content = editParam.getContent();
            String title = editParam.getTitle();
            String registerPublishId = editParam.getRegisterPublishId();
            if (StringUtils.isAnyEmpty(content, title, registerPublishId, enterpriseId)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("缺失必要参数");
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneByIdAndEnterpriseId(registerPublishId, enterpriseId);
            if (registerPublishModel == null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("不能存在考试信息");
            }
            String uniqueId = UniqueUtil.getUniqueId();
            editParam.setId(uniqueId);
            editParam.setCreateBy(loginUsername);
            editParam.setUpdateBy(loginUsername);
            editParam.setEnterpriseId(enterpriseId);
            Long timeMillis = DateUtil.currentTimeMillis();
            editParam.setCreateAt(timeMillis);
            editParam.setUpdateAt(timeMillis);
            registerPublishAnnouncementMapper.insert(editParam);
            SysJob sysJob = new SysJob();
            sysJob.setJobId(uniqueId);
            sysJob.setJobGroup(REGISTER_PUBLISH_ANNOUNCEMENT_TASK_JOB_GROUP);
            sysJob.setStatus("0");
            sysJob.setConcurrent("1");
            sysJob.setJobName(title);
            // 10分钟转换为毫秒
            long tenMinutesInMillis = 10 * 60 * 1000;
            sysJob.setCronExpression(CronUtils.convertTimestampToCron(tenMinutesInMillis + timeMillis));
            String invokeTarget = String.format(REGISTER_PUBLISH_ANNOUNCEMENT_TASK_INVOKE_TARGET, registerPublishId, uniqueId);
            sysJob.setInvokeTarget(invokeTarget);
            sysJob.setMisfirePolicy("3");
            sysJob.setCreateBy(loginUsername);
            sysJob.setUpdateBy(loginUsername);
            sysJob.setCreateAt(timeMillis);
            sysJob.setUpdateAt(timeMillis);
            sysJobService.insertSendJob(sysJob);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("添加发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "添加发生异常");
        }
        return result;
    }

    public I18nResult<PageModel<RegisterPublishAnnouncementModel>> queryByPageAndCondition(RegisterPublishAnnouncementQueryParam queryParam) {
        I18nResult<PageModel<RegisterPublishAnnouncementModel>> result = I18nResult.newInstance();
        try {
            PageMethod.startPage(queryParam.getPageNum(), queryParam.getPageSize());
            List<RegisterPublishAnnouncementModel> announcementModels = registerPublishAnnouncementMapper.queryByPageAndCondition(queryParam);
            result.succeed().setData(new PageModel<>(announcementModels));
        } catch (Exception e) {
            log.error("分页条件查询发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "分页条件查询发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> put(RegisterPublishAnnouncementEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String id = editParam.getId();
            String loginUsername = SecurityUtil.getLoginUsername();
            String enterpriseId = SecurityUtil.getEnterpriseId();
            String title = editParam.getTitle();
            String content = editParam.getContent();
            if (StringUtils.isAnyEmpty(enterpriseId, title, content, id)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            editParam.setEnterpriseId(enterpriseId);
            editParam.setUpdateBy(loginUsername);
            Long timeMillis = DateUtil.currentTimeMillis();
            editParam.setUpdateAt(timeMillis);
            int rows = registerPublishAnnouncementMapper.updateByIdAndEnterpriseId(editParam);
            if (rows > 0) {
                sysJobService.updateCronExpressionByJobId(id, CronUtils.convertTimestampToCron(TEN_MINUTES_IN_MILLIS + timeMillis));
            }
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("修改发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "修改发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> deleteById(String id) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(enterpriseId, id)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            int rows = registerPublishAnnouncementMapper.deleteByIdAndEnterpriseId(id, enterpriseId);
            if (rows > 0) {
                sysJobService.deleteJobById(id);
            }
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("根据id删除发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "根据id删除发生异常");
        }
        return result;
    }

    public I18nResult<RegisterPublishAnnouncementModel> queryOneById(String id) {
        I18nResult<RegisterPublishAnnouncementModel> result = I18nResult.newInstance();
        try {
            RegisterPublishAnnouncementModel announcementModel = registerPublishAnnouncementMapper.queryOneById(id);
            result.succeed().setData(announcementModel);
        } catch (Exception e) {
            log.error("根据id查询详情发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "根据id查询详情发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> newsAdd(RegisterPublishAnnouncementEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String loginUsername = SecurityUtil.getLoginUsername();
            String enterpriseId = SecurityUtil.getEnterpriseId();
            String content = editParam.getContent();
            String title = editParam.getTitle();
            if (StringUtils.isAnyEmpty(content, title, enterpriseId)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("缺失必要参数");
            }
            String uniqueId = UniqueUtil.getUniqueId();
            editParam.setId(uniqueId);
            editParam.setCreateBy(loginUsername);
            editParam.setUpdateBy(loginUsername);
            editParam.setEnterpriseId(enterpriseId);
            editParam.setType(RegisterPublishAnnouncementTypeEnum.NEWS_UPDATES);
            Long timeMillis = DateUtil.currentTimeMillis();
            editParam.setCreateAt(timeMillis);
            editParam.setUpdateAt(timeMillis);
            registerPublishAnnouncementMapper.insert(editParam);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("添加发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "添加发生异常");
        }
        return result;
    }
}
