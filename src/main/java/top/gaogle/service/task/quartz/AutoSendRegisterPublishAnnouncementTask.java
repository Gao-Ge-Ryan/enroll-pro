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
import top.gaogle.framework.util.DateUtil;
import top.gaogle.framework.util.UniqueUtil;
import top.gaogle.pojo.enums.RegisterInfoStatusEnum;
import top.gaogle.pojo.enums.RegisterPublishAnnouncementTypeEnum;
import top.gaogle.pojo.model.RegisterPublishModel;
import top.gaogle.pojo.param.RegisterPublishAnnouncementEditParam;
import top.gaogle.service.EmailService;

import java.util.List;

import static top.gaogle.common.RegisterConst.REGISTER_INFO_TABLE_NAME;

/**
 * 定时任务 自动发布公告通知
 *
 * @author gaogle
 */
@Component("autoSendRegisterPublishAnnouncementTask")
public class AutoSendRegisterPublishAnnouncementTask {
    protected final Logger log = LoggerFactory.getLogger(AutoSendRegisterPublishAnnouncementTask.class);
    @Value("${spring.mail.username}")
    public String platformMail;

    private final DynamicRegisterInfoMapper dynamicRegisterInfoMapper;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final RegisterPublishAnnouncementMapper registerPublishAnnouncementMapper;
    private final RegisterPublishMapper registerPublishMapper;

    public AutoSendRegisterPublishAnnouncementTask(DynamicRegisterInfoMapper dynamicRegisterInfoMapper, EmailService emailService, TemplateEngine templateEngine, RegisterPublishAnnouncementMapper registerPublishAnnouncementMapper, RegisterPublishMapper registerPublishMapper) {
        this.dynamicRegisterInfoMapper = dynamicRegisterInfoMapper;
        this.emailService = emailService;
        this.templateEngine = templateEngine;
        this.registerPublishAnnouncementMapper = registerPublishAnnouncementMapper;
        this.registerPublishMapper = registerPublishMapper;
    }

    public void task(String registerPublishId, String typeName) {
        String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
        RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneById(registerPublishId);
        if (registerPublishModel == null) {
            log.info("定时任务自动发布公告未查询到发布考试:{}", registerPublishId);
            return;
        }
        RegisterPublishAnnouncementEditParam editParam = new RegisterPublishAnnouncementEditParam();
        String name;
        String startTime;
        String endTime;
        String operate;
        if (RegisterPublishAnnouncementTypeEnum.REGISTER.name().equals(typeName)) {
            name = RegisterPublishAnnouncementTypeEnum.REGISTER.title();
            startTime = DateUtil.timeMillisFormatter(registerPublishModel.getStartAt(), "yyyy-MM-dd HH:mm:ss");
            endTime = DateUtil.timeMillisFormatter(registerPublishModel.getEndAt(), "yyyy-MM-dd HH:mm:ss");
            operate = "报名";
            editParam.setType(RegisterPublishAnnouncementTypeEnum.REGISTER);
        } else if (RegisterPublishAnnouncementTypeEnum.TICKET.name().equals(typeName)) {
            name = RegisterPublishAnnouncementTypeEnum.TICKET.title();
            startTime = DateUtil.timeMillisFormatter(registerPublishModel.getTicketStartAt(), "yyyy-MM-dd HH:mm:ss");
            endTime = DateUtil.timeMillisFormatter(registerPublishModel.getTicketEndAt(), "yyyy-MM-dd HH:mm:ss");
            operate = "打印证件";
            editParam.setType(RegisterPublishAnnouncementTypeEnum.TICKET);
        } else if (RegisterPublishAnnouncementTypeEnum.SCORE.name().equals(typeName)) {
            name = RegisterPublishAnnouncementTypeEnum.SCORE.title();
            startTime = DateUtil.timeMillisFormatter(registerPublishModel.getScoreStartAt(), "yyyy-MM-dd HH:mm:ss");
            endTime = DateUtil.timeMillisFormatter(registerPublishModel.getScoreEndAt(), "yyyy-MM-dd HH:mm:ss");
            operate = "查询成绩";
            editParam.setType(RegisterPublishAnnouncementTypeEnum.SCORE);
        } else {
            log.info("定时任务自动发布公告类型不对考试ID:{}", registerPublishId);
            return;
        }
        String enterpriseId = registerPublishModel.getEnterpriseId();
        String title = registerPublishModel.getTitle();
        String endTitle = title + name;

        String uniqueId = UniqueUtil.getUniqueId();
        editParam.setId(uniqueId);
        editParam.setEnterpriseId(enterpriseId);
        editParam.setTitle(endTitle);
        String noticeTemplate = "<div style='background-color:#f4f7fa; border:1px solid #dcdfe6; padding:20px; border-radius:8px; font-family:Arial, sans-serif;'>" +
                "<h2 style='color:#2c3e50; text-align:center;'>#{endTitle}</h2>" +
                "<p style='font-size:16px; color:#333;'>尊敬的用户，您好！</p>" +
                "<p style='font-size:16px; color:#333;'>本次公告内容如下：</p>" +
                "<p style='font-size:16px; color:#333;'>#{title} #{operate}将在<strong>#{startTime}</strong>至<strong>#{endTime}</strong>期间进行，届时我们建议您在此期间做好相应准备。</p>" +
                "<p style='font-size:16px; color:#333;'>感谢您的理解与支持！</p>" +
                "<p style='font-size:14px; color:#999; text-align:right;'>注：此公告内容由系统自动生成，若您有疑问请联系我们！</p>" +
                "</div>";

        // 使用String.replace()来替换占位符
        String noticeHtml = noticeTemplate.replace("#{endTitle}", endTitle).replace("#{title}", title)
                .replace("#{operate}", operate).replace("#{startTime}", startTime).replace("#{endTime}", endTime);
        editParam.setContent(noticeHtml);
        editParam.setRegisterPublishId(registerPublishId);
        Long timeMillis = DateUtil.currentTimeMillis();
        editParam.setCreateAt(timeMillis);
        editParam.setUpdateAt(timeMillis);
        registerPublishAnnouncementMapper.insert(editParam);
        List<String> createBys = dynamicRegisterInfoMapper.queryCreateBysByIdAndStatus(tableName, RegisterInfoStatusEnum.VALID.value());
        if (!CollectionUtils.isEmpty(createBys)) {
            for (String createBy : createBys) {
                if (StringUtils.isNotEmpty(createBy)) {
                    try {
                        Context context = new Context();
                        context.setVariable("title", endTitle);
                        context.setVariable("noticeHtml", noticeHtml);
                        String content = templateEngine.process("autoSendRegisterPublishAnnouncementTask.html", context);
                        emailService.send(GaogleConfig.getSystemName(), platformMail, createBy, endTitle, content, true, null, null, null);
                    } catch (Exception e) {
                        log.error("定时任务自动公告个人{}发送邮件通知异常", createBy, e);
                    }

                }
            }
        }
    }

}
