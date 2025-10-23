package top.gaogle.service.task.quartz;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import top.gaogle.dao.master.RegisterPublishAnnouncementMapper;
import top.gaogle.dao.master.RegisterPublishMapper;
import top.gaogle.dao.slave.DynamicRegisterInfoMapper;
import top.gaogle.framework.config.GaogleConfig;
import top.gaogle.pojo.enums.RegisterInfoStatusEnum;
import top.gaogle.pojo.model.RegisterPublishAnnouncementModel;
import top.gaogle.pojo.model.RegisterPublishModel;
import top.gaogle.service.EmailService;

import java.util.List;

import static top.gaogle.common.RegisterConst.REGISTER_INFO_TABLE_NAME;

/**
 * 定时任务 发布公告通知
 *
 * @author gaogle
 */
@Component("registerPublishAnnouncementTask")
public class RegisterPublishAnnouncementTask {
    protected final Logger log = LoggerFactory.getLogger(RegisterPublishAnnouncementTask.class);

    @Value("${spring.mail.username}")
    public String platformMail;

    private final DynamicRegisterInfoMapper dynamicRegisterInfoMapper;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final RegisterPublishAnnouncementMapper registerPublishAnnouncementMapper;
    private final RegisterPublishMapper registerPublishMapper;

    public RegisterPublishAnnouncementTask(DynamicRegisterInfoMapper dynamicRegisterInfoMapper, EmailService emailService, TemplateEngine templateEngine, RegisterPublishAnnouncementMapper registerPublishAnnouncementMapper, RegisterPublishMapper registerPublishMapper) {
        this.dynamicRegisterInfoMapper = dynamicRegisterInfoMapper;
        this.emailService = emailService;
        this.templateEngine = templateEngine;
        this.registerPublishAnnouncementMapper = registerPublishAnnouncementMapper;
        this.registerPublishMapper = registerPublishMapper;
    }

    public void task(String registerPublishId, String announcementId) {
        RegisterPublishAnnouncementModel announcementModel = registerPublishAnnouncementMapper.queryOneById(announcementId);
        RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneById(registerPublishId);
        if (registerPublishModel == null || announcementModel == null) {
            log.info("定时任务手动发布公告未查询到发布考试或公告:{}-{}", registerPublishId, announcementId);
            return;
        }
        String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
        List<String> createBys = dynamicRegisterInfoMapper.queryCreateBysByIdAndStatus(tableName, RegisterInfoStatusEnum.VALID.value());
        if (!CollectionUtils.isEmpty(createBys)) {
            for (String createBy : createBys) {
                if (StringUtils.isNotEmpty(createBy)) {
                    try {
                        Context context = new Context();
                        String noticeHtml = announcementModel.getContent();
                        context.setVariable("title", announcementModel.getTitle());
                        context.setVariable("noticeHtml", noticeHtml);
                        String content = templateEngine.process("registerPublishAnnouncementTask.html", context);
                        emailService.send(GaogleConfig.getSystemName(), platformMail, createBy, registerPublishModel.getTitle() + " 新公告提醒", content, true, null, null, null);
                    } catch (Exception e) {
                        log.error("定时任务手动公告个人{}发送邮件通知异常", createBy, e);
                    }

                }
            }
        }
    }

}
