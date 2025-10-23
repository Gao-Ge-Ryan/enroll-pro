package top.gaogle.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.EasyExcelFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import top.gaogle.common.RegisterConst;
import top.gaogle.dao.master.*;
import top.gaogle.dao.slave.DynamicRegisterInfoMapper;
import top.gaogle.framework.config.GaogleConfig;
import top.gaogle.framework.config.MinioConfig;
import top.gaogle.framework.i18n.I18ResultCode;
import top.gaogle.framework.i18n.I18nResult;
import top.gaogle.framework.util.*;
import top.gaogle.pojo.dto.*;
import top.gaogle.pojo.enums.HttpStatusEnum;
import top.gaogle.pojo.enums.InterviewTicketTemplateFlagEnum;
import top.gaogle.pojo.enums.RegisterInfoStatusEnum;
import top.gaogle.pojo.enums.TicketTemplateFlagEnum;
import top.gaogle.pojo.model.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static top.gaogle.common.RegisterConst.*;

@Service
public class FileService extends SuperService {
    @Value("${minio.bucket-name-public}")
    private String bucketName;

    @Value("${minio.picture-url}")
    private String minioPictureUrl;

    @Value("${minio.file-url}")
    private String minioFileUrl;

    private final MinioUtil minioUtil;
    private final DynamicRegisterInfoMapper dynamicRegisterInfoMapper;
    private final RegisterPublishMapper registerPublishMapper;
    private final MinioConfig minioConfig;
    private final EmailService emailService;
    private final PublishSpotMapper publishSpotMapper;
    private final TransactionTemplate slaveTransactionTemplate;
    private final TemplateEngine templateEngine;
    private final TicketTemplateMapper ticketTemplateMapper;
    private final InterviewTicketTemplateMapper interviewTicketTemplateMapper;
    private final ActivityInfoMapper activityInfoMapper;

    @Autowired
    public FileService(MinioUtil minioUtil, DynamicRegisterInfoMapper dynamicRegisterInfoMapper, RegisterPublishMapper registerPublishMapper, MinioConfig minioConfig, EmailService emailService, PublishSpotMapper publishSpotMapper, @Qualifier("slaveTransactionTemplate") TransactionTemplate slaveTransactionTemplate, TemplateEngine templateEngine, TicketTemplateMapper ticketTemplateMapper, InterviewTicketTemplateMapper interviewTicketTemplateMapper, ActivityInfoMapper activityInfoMapper) {
        this.minioUtil = minioUtil;
        this.dynamicRegisterInfoMapper = dynamicRegisterInfoMapper;
        this.registerPublishMapper = registerPublishMapper;
        this.minioConfig = minioConfig;
        this.emailService = emailService;
        this.publishSpotMapper = publishSpotMapper;
        this.slaveTransactionTemplate = slaveTransactionTemplate;
        this.templateEngine = templateEngine;
        this.ticketTemplateMapper = ticketTemplateMapper;
        this.interviewTicketTemplateMapper = interviewTicketTemplateMapper;
        this.activityInfoMapper = activityInfoMapper;
    }

    public I18nResult<String> upload(MultipartFile file) {
        I18nResult<String> result = I18nResult.newInstance();
        try {
            String filename = file.getOriginalFilename();
            assert filename != null;
            String type = filename.substring(filename.lastIndexOf(".") + 1);

            String magicNumberType = FileUtil.getFileTypeByMagicNumber(file);
            if (StringUtils.isEmpty(magicNumberType)) {
                magicNumberType = type;
            }
            String minioFileType = FileUtil.getMinioFileType(magicNumberType);
            String objectName = UniqueUtil.getUniqueId() + RegisterConst.DOT + magicNumberType;
            if (StringUtils.isEmpty(minioFileType)) {
                minioFileType = "application/octet-stream";
            }
            minioUtil.putObject(bucketName, file, objectName, minioFileType, FILE);
            result.succeed().setData(objectName);
        } catch (Exception e) {
            log.error("文件上传发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "文件上传发生异常");
        }
        return result;

    }

    public I18nResult<String> getObjectUrl(String objectName) {
        I18nResult<String> result = I18nResult.newInstance();
        try {
            String objectUrl = minioUtil.getExpiryObjectUrl(bucketName, objectName, 1, TimeUnit.HOURS, PICTURE);
            result.succeed().setData(objectUrl);
        } catch (Exception e) {
            log.error("图片上传发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "图片上传发生异常");
        }
        return result;
    }

    public I18nResult<String> uploadPicture(MultipartFile file) {
        I18nResult<String> result = I18nResult.newInstance();
        try {
            String magicNumberType = FileUtil.getFileTypeByMagicNumber(file);
            if (!("jpg".equals(magicNumberType) || "png".equals(magicNumberType) || "gif".equals(magicNumberType)
                    || "webp".equals(magicNumberType) || "jpeg".equals(magicNumberType) || "svg".equals(magicNumberType))) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage(I18ResultCode.MESSAGE, "仅支持上传jpg、jpeg、png、gif、webp、svg格式文件");
            }
            String minioFileType = FileUtil.getMinioFileType(magicNumberType);
            String objectName = UniqueUtil.getUniqueId() + RegisterConst.DOT + magicNumberType;
            minioUtil.putObject(bucketName, file, objectName, minioFileType, "picture");
            result.succeed().setData(objectName);
        } catch (Exception e) {
            log.error("文件上传发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "文件上传发生异常");
        }
        return result;
    }

    public void obtainAdmissionTicket(HttpServletResponse response, String registerPublishId) {
        try {
            if (StringUtils.isAnyEmpty(registerPublishId)) {
                FileUtil.response2Json(response, "缺失必要参数");
                return;
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneById(registerPublishId);

            if (registerPublishModel == null) {
                FileUtil.response2Json(response, "未查询到考试");
                return;
            }

            if (!Boolean.TRUE.equals(registerPublishModel.getTicketFlag())) {
                FileUtil.response2Json(response, "未开启打印证件信息");
                return;
            }

            Long timeMillis = DateUtil.currentTimeMillis();
            if (timeMillis < registerPublishModel.getTicketStartAt()) {
                FileUtil.response2Json(response, "不在打印证件时间范围内");
                return;
            }
            Boolean activityFlag = registerPublishModel.getActivityFlag();
            if (!Boolean.TRUE.equals(activityFlag) && timeMillis > registerPublishModel.getTicketEndAt()) {
                FileUtil.response2Json(response, "不在打印证件时间范围内");
                return;
            }
            if (Boolean.TRUE.equals(activityFlag)) {
                List<ActivityInfoModel> activityInfoModels = activityInfoMapper.queryByRegisterPublishId(registerPublishId);
                if (!CollectionUtils.isEmpty(activityInfoModels)) {
                    ActivityInfoModel lastModel = activityInfoModels.get(activityInfoModels.size() - 1);
                    Long activityEndAt = lastModel.getActivityEndAt();
                    if (timeMillis > activityEndAt) {
                        FileUtil.response2Json(response, "不在打印证件时间范围内");
                        return;
                    }
                }
            }

            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            String loginUsername = SecurityUtil.getLoginUsername();
            String templateCopy = registerPublishModel.getTemplateCopy();
            List<String> fields = new ArrayList<>();
            if (StringUtils.isNotEmpty(templateCopy)) {
                ObjectNode templateCopyNode = JsonUtil.parseObjectNode(templateCopy);
                Iterator<Map.Entry<String, JsonNode>> iterator = templateCopyNode.fields();
                while (iterator.hasNext()) {
                    Map.Entry<String, JsonNode> next = iterator.next();
                    String key = next.getKey();
                    JsonNode jsonNode = next.getValue();
                    String column = jsonNode.get(KEY).asText();
                    Boolean keyValue = jsonNode.get(TEMPLATE_KEY_VALUE).asBoolean(false);
                    String remark = jsonNode.get(TEMPLATE_REMARK).asText();
                    fields.add(column + AS + key);
                }
            }

            for (String key : PRESET_DYNAMIC_FIELD_MAP.keySet()) {
                fields.add(key + AS + StringUtil.snakeToCamelCase(key));
            }
            List<Map<String, Object>> conditions = new ArrayList<>();
            Map<String, Object> condition = new HashMap<>();
            condition.put(KEY, COLUMN_CREATE_BY);
            condition.put(VALUE, loginUsername);
            condition.put(MATCH_TYPE, MATCH_TYPE_EXACT); // 使用模糊匹配
            conditions.add(condition);
            List<Map<String, Object>> objectNodes = dynamicRegisterInfoMapper.selectDynamic(tableName, fields, conditions);

            if (objectNodes == null || CollectionUtils.isEmpty(objectNodes)) {
                FileUtil.response2Json(response, "未查询到考生信息");
                return;
            }
            Map<String, Object> objectMap = objectNodes.get(0);
            if (!RegisterInfoStatusEnum.VALID.value().equals(objectMap.get(StringUtil.snakeToCamelCase(COLUMN_STATUS)))) {
                FileUtil.response2Json(response, "报名信息无效状态，禁止操作");
                return;
            }
            Object spotId = objectMap.getOrDefault(StringUtil.snakeToCamelCase(COLUMN_SPOT_ID), null);
            if ((!Boolean.TRUE.equals(registerPublishModel.getAllocateSpotFlag()) || spotId == null || StringUtils.isEmpty(spotId.toString()))) {
                log.error("打印准考证了但是未分配考点registerPublishId:{},loginUsername:{}", registerPublishId, loginUsername);
                String title = registerPublishModel.getTitle();
                String endTitle = title + " 分配考点提醒";
                String endMessage = "已经有报名者开始打印证件信息，但您仍未分配考点，请及时分配考点，避免影响进程";
                String noticeTemplate = "<p style='font-family: Arial, sans-serif; font-size: 16px; color: #333;'>"
                        + "【<a href='#{clientUrl}' target='_blank' style='text-decoration: none; font-weight: bold; color: #3399FF;'>#{systemName}</a>】您发布的#{title}（编号：#{registerPublishId}"
                        + "）#{endMessage}，更多信息请访问官方网址！</p>"
                        + "<div style='margin-top: 15px; text-align: center;'>"
                        + "<a href='#{clientUrl}' target='_blank' "
                        + "style='display: inline-block; padding: 10px 20px; background-color: rgba(0, 123, 255, 0.1); color: #336699; font-weight: 500; "
                        + "border-radius: 5px; text-decoration: none; border: 1px solid rgba(0,123,255,0.3);'>访问官网</a></div>";
                String noticeHtml = noticeTemplate.replace("#{systemName}", GaogleConfig.getSystemName()).replace("#{title}", title)
                        .replace("#{registerPublishId}", registerPublishId).replace("#{endMessage}", endMessage).replace("#{clientUrl}", GaogleConfig.getClientUrl());
                Context context = new Context();
                context.setVariable("title", endTitle);
                context.setVariable("noticeHtml", noticeHtml);
                String content = templateEngine.process(GENERAL_HTML, context);
                I18nResult<Boolean> sendResult = emailService.sendHTML(registerPublishModel.getCreateBy(), endTitle, content);
                if (sendResult.isFailed()) {
                    log.error("发送分配考点邮件通知失败");
                }
                FileUtil.response2Json(response, "暂未分配考点，已经邮件通知管理者分配考点，请您稍后重试或直接联系我们！");
                return;
            }

            TicketTemplateFlagEnum ticketTemplateFlag = registerPublishModel.getTicketTemplateFlag();
            TicketTemplateModel templateModel = ticketTemplateMapper.queryOneByFlag(ticketTemplateFlag);
            String fieldContent = templateModel.getFieldContent();
//            String textContent = templateModel.getTextContent();
            String noticeHtml = registerPublishModel.getTicketAttach();

            //构造模板引擎
            ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
            //模板所在目录，相对于当前classloader的classpath
            resolver.setPrefix("templates/ticket/");
            //模板文件后缀
            resolver.setSuffix(".html");
            SpringTemplateEngine templateEngine = new SpringTemplateEngine();
            templateEngine.setTemplateResolver(resolver);
            // 预定义报名模板字段取值
            Object name = objectMap.getOrDefault(StringUtil.snakeToCamelCase(COLUMN_NAME), null);
            Object admissionTicketNumber = objectMap.getOrDefault(StringUtil.snakeToCamelCase(COLUMN_ADMISSION_TICKET_NUMBER), null);
            Object gender = objectMap.getOrDefault(StringUtil.snakeToCamelCase(COLUMN_GENDER), null);
            Object idNumber = objectMap.getOrDefault(StringUtil.snakeToCamelCase(COLUMN_ID_NUMBER), null);
            Object spot = objectMap.getOrDefault(StringUtil.snakeToCamelCase(COLUMN_SPOT), null);
            Object spotAddress = objectMap.getOrDefault(StringUtil.snakeToCamelCase(COLUMN_SPOT_ADDRESS), null);
            Object roomNumber = objectMap.getOrDefault(StringUtil.snakeToCamelCase(COLUMN_ROOM_NUMBER), null);
            Object seatNumber = objectMap.getOrDefault(StringUtil.snakeToCamelCase(COLUMN_SEAT_NUMBER), null);
            Object photo = objectMap.getOrDefault(StringUtil.snakeToCamelCase(COLUMN_PHOTO), null);

            //构造上下文(Model)
            Context context = new Context();
            String photoUrl = minioPictureUrl + (photo == null ? "" : photo);
            context.setVariable("photoUrl", photoUrl);
            context.setVariable("registerPublishTitle", registerPublishModel.getTitle());
            context.setVariable("name", name);
            context.setVariable("admissionTicketNumber", admissionTicketNumber);
            context.setVariable("gender", gender);
            context.setVariable("idNumber", idNumber);
            context.setVariable("spot", spot);
            context.setVariable("spotAddress", spotAddress);
            context.setVariable("roomNumber", roomNumber);
            context.setVariable("seatNumber", seatNumber);
            // 报名模板动态字段
            if (StringUtils.isNotEmpty(fieldContent)) {
                ObjectNode fieldContentNode = JsonUtil.parseObjectNode(fieldContent);
                Iterator<Map.Entry<String, JsonNode>> iterator = fieldContentNode.fields();
                while (iterator.hasNext()) {
                    Map.Entry<String, JsonNode> next = iterator.next();
                    String key = next.getKey();
                    context.setVariable(key, objectMap.getOrDefault(key, null));
                }
            }

            if (Boolean.TRUE.equals(activityFlag)) {
                List<ActivityInfoModel> activityInfoModels = activityInfoMapper.queryByRegisterPublishId(registerPublishId);
                List<TicketActivityInfoDTO> activityInfoDTOS = new ArrayList<>();
                if (!CollectionUtils.isEmpty(activityInfoModels)) {
                    for (ActivityInfoModel activityInfoModel : activityInfoModels) {
                        String subject = activityInfoModel.getSubject();
                        Long activityStartAt = activityInfoModel.getActivityStartAt();
                        Long activityEndAt = activityInfoModel.getActivityEndAt();
                        TicketActivityInfoDTO ticketActivityInfoDTO = new TicketActivityInfoDTO();
                        ZoneId zone = ZoneId.of("Asia/Shanghai"); // 替换为你的时区
                        // 日期格式
                        LocalDate date = Instant.ofEpochMilli(activityStartAt).atZone(zone).toLocalDate();
                        String examDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        // 时间格式
                        LocalTime startTime = Instant.ofEpochMilli(activityStartAt).atZone(zone).toLocalTime();
                        LocalTime endTime = Instant.ofEpochMilli(activityEndAt).atZone(zone).toLocalTime();
                        String examTime = startTime.format(DateTimeFormatter.ofPattern("H:mm")) + "-" +
                                endTime.format(DateTimeFormatter.ofPattern("H:mm"));
                        ticketActivityInfoDTO.setSubject(subject);
                        ticketActivityInfoDTO.setDate(examDate);
                        ticketActivityInfoDTO.setTime(examTime);
                        activityInfoDTOS.add(ticketActivityInfoDTO);
                    }
                }
                context.setVariable("activityInfoDTOS", activityInfoDTOS);
            }
            context.setVariable("noticeHtml", noticeHtml);
            //渲染模板
            String example = templateEngine.process(ticketTemplateFlag.name(), context);
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFont(() -> this.getClass().getClassLoader().getResourceAsStream("static/fonts/simsun.ttf"), "simsun");
            builder.useFastMode();
            builder.withHtmlContent(example, ResourceUtils.getURL("classpath:static/images/pdf/").toString());
            response.setContentType("application/pdf");
            String pdfFileName = "准考证.pdf";
            String encodedFileName = URLEncoder.encode(pdfFileName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

            // 获取输出流
            try (OutputStream os = response.getOutputStream()) {
                builder.toStream(os);
                builder.run();
                dynamicRegisterInfoMapper.updateTicketDownloadCount(tableName, loginUsername);
            } catch (Exception e) {
                log.error("builder.run() 失败:", e);
            }
        } catch (Exception e) {
            log.error("打印证件信息发生异常:", e);
            FileUtil.response2Json(response, "打印证件信息发生异常");
        }
    }


    public void exportRegisterInfo(HttpServletResponse response, String registerPublishId, RegisterInfoStatusEnum status) {
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(enterpriseId, registerPublishId)) {
                FileUtil.response2Json(response, "缺失必要参数");
                return;
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneByIdAndEnterpriseId(registerPublishId, enterpriseId);
            if (registerPublishModel == null) {
                FileUtil.response2Json(response, "发布信息不存在");
                return;
            }
            List<String> timestamps = new ArrayList<>(Arrays.asList("createAt", "updateAt"));
            List<String> jsons = new ArrayList<>();
            List<String> arrayJsons = new ArrayList<>();
            String title = registerPublishModel.getTitle();
            List<String> fields = new ArrayList<>();
            List<String> camelCaseFields = new ArrayList<>();
            // 定义动态表头
            List<List<String>> headList = new ArrayList<>();
            for (Map.Entry<String, String> entry : PRESET_DYNAMIC_FIELD_MAP.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                String camelCase = StringUtil.snakeToCamelCase(key);
                fields.add(key + AS + camelCase);
                List<String> tmp = new ArrayList<>();
                tmp.add(value);
                headList.add(tmp);
                camelCaseFields.add(camelCase);
            }
            List<Map<String, Object>> conditions = new ArrayList<>();
            if (status != null) {
                Map<String, Object> condition = new HashMap<>();
                condition.put(KEY, COLUMN_STATUS);
                condition.put(VALUE, status.value());
                condition.put(MATCH_TYPE, MATCH_TYPE_EXACT);
                conditions.add(condition);
            }
            String templateCopy = registerPublishModel.getTemplateCopy();
            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            if (StringUtils.isNotEmpty(templateCopy)) {
                ObjectNode templateCopyNode = JsonUtil.parseObjectNode(templateCopy);
                Iterator<Map.Entry<String, JsonNode>> iterator = templateCopyNode.fields();
                while (iterator.hasNext()) {
                    Map.Entry<String, JsonNode> next = iterator.next();
                    JsonNode jsonNode = next.getValue();
                    String column = jsonNode.get(TEMPLATE_KEY).asText();
                    String remark = jsonNode.get(TEMPLATE_REMARK).asText();
                    String formatType = jsonNode.get(TEMPLATE_FORMAT_TYPE).asText();
                    String camelCase = StringUtil.snakeToCamelCase(column);
                    fields.add(column + AS + camelCase);
                    List<String> tmp = new ArrayList<>();
                    tmp.add(remark);
                    headList.add(tmp);
                    camelCaseFields.add(camelCase);
                    if (FORMAT_TYPE_TIMESTAMP.equals(formatType)) {
                        timestamps.add(camelCase);
                    }
                    if (FORMAT_TYPE_JSON.equals(formatType)) {
                        jsons.add(camelCase);
                    }
                    if (FORMAT_TYPE_ARRAY_JSON.equals(formatType)) {
                        arrayJsons.add(camelCase);
                    }
                }
            }
            List<Map<String, Object>> dynamics = dynamicRegisterInfoMapper.selectDynamic(tableName, fields, conditions);
            List<List<Object>> data = new ArrayList<>();


            for (Map<String, Object> dynamic : dynamics) {
                List<Object> row = new ArrayList<>();
                for (String field : camelCaseFields) {
                    Object value = dynamic.getOrDefault(field, null);
                    if (timestamps.contains(field) && value != null && StringUtil.isNotEmpty(value.toString())) {
                        value = DateUtil.timeMillisFormatter(Long.parseLong(value.toString()), "yyyy-MM-dd HH:mm:ss");
                    }
                    if (jsons.contains(field) && value != null && StringUtil.isNotEmpty(value.toString())) {
                        ObjectNode objectNode = JsonUtil.parseObjectNode(value.toString());
                        if (objectNode != null) {
                            value = objectNode.path(FORMAT_TYPE_SHOW_VALUE).asText("");
                        }
                    }
                    if (arrayJsons.contains(field) && value != null && StringUtil.isNotEmpty(value.toString())) {
                        ArrayNode arrayNode = JsonUtil.parseArrayNode(value.toString());
                        String showStr = "";
                        for (JsonNode jsonNode : arrayNode) {
                            String temp = jsonNode.path(FORMAT_TYPE_SHOW_VALUE).asText("");
                            showStr += temp + "\n";
                        }
                        value = showStr;
                    }
                    if (value != null && StringUtil.isNotEmpty(value.toString()) && StringUtil.snakeToCamelCase(COLUMN_PHOTO).equals(field)) {
                        value = new URL(minioPictureUrl + value);
                    }
                    row.add(value);
                }
                data.add(row);
            }
            String name = title + "报名者详细信息";
            // 设置响应的内容类型为 Excel 格式
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 设置文件名并进行 URL 编码
            String fileName = URLEncoder.encode(name, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            // 使用 EasyExcel 将数据写入响应的输出流
            EasyExcel.write(response.getOutputStream())
                    .head(headList)
                    .sheet(name)
                    .doWrite(data);

        } catch (Exception e) {
            log.error("下载excel异常:", e);
            FileUtil.response2Json(response, "下载excel异常");

        }
    }


//    public void exportRegisterBaseInfo(HttpServletResponse response, String registerPublishId) {
//        try {
//            String enterpriseId = SecurityUtil.getEnterpriseId();
//            if (StringUtils.isAnyEmpty(registerPublishId, enterpriseId)) {
//                FileUtil.response2Json(response, "缺失必要参数");
//                return;
//            }
//
//            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneByIdAndEnterpriseId(registerPublishId, enterpriseId);
//            if (registerPublishModel == null) {
//                FileUtil.response2Json(response, "未查询到报名发布信息");
//                return;
//            }
//            String title = registerPublishModel.getTitle();
//            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
//            List<DynamicRegisterInfoModel> infoModels = dynamicRegisterInfoMapper.selectBaseInfoByRegisterPublishId(tableName, registerPublishId);
//            List<DynamicRegisterInfoExcelDTO> excelDTOS = new ArrayList<>();
//            for (DynamicRegisterInfoModel infoModel : infoModels) {
//                DynamicRegisterInfoExcelDTO excelDTO = new DynamicRegisterInfoExcelDTO();
//                excelDTO.setId(infoModel.getId());
//                excelDTO.setName(infoModel.getName());
//                excelDTO.setIdNumber(infoModel.getIdNumber());
//                excelDTO.setAdmissionTicketNumber(infoModel.getAdmissionTicketNumber());
//                excelDTO.setPhoto(new URL(minioConfig.getEndpoint() + ":" + minioConfig.getPort() + "/" +
//                        minioConfig.getBucketNamePublic() + "/picture/" + infoModel.getPhoto()));
//                excelDTO.setPhoneNumber(infoModel.getPhoneNumber());
//                excelDTO.setEmail(infoModel.getEmail());
//                excelDTO.setSpot(infoModel.getSpot());
//                excelDTO.setSpotAddress(infoModel.getSpotAddress());
//                excelDTO.setRoomNumber(infoModel.getRoomNumber());
//                excelDTO.setSeatNumber(infoModel.getSeatNumber());
//                excelDTO.setGender(infoModel.getGender());
//                excelDTO.setEducation(infoModel.getEducation());
//                excelDTO.setMajor(infoModel.getMajor());
//                excelDTO.setScore(infoModel.getScore());
//                excelDTO.setStatus(infoModel.getStatus());
//                excelDTO.setApprove(DynamicRegisterInfoApproveEnum.fromValue(infoModel.getApprove()).title());
//                excelDTO.setReason(infoModel.getReason());
//                excelDTO.setCreateBy(infoModel.getCreateBy());
//                excelDTO.setCreateAt(DateUtil.timeMillisFormatter(infoModel.getCreateAt(), "yyyy-MM-dd HH:mm:ss"));
//                excelDTO.setUpdateBy(infoModel.getUpdateBy());
//                excelDTO.setUpdateAt(DateUtil.timeMillisFormatter(infoModel.getUpdateAt(), "yyyy-MM-dd HH:mm:ss"));
//                excelDTOS.add(excelDTO);
//            }
//            String name = title + "报名者基础信息";
//            // 设置响应的内容类型为 Excel 格式
//            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//            response.setCharacterEncoding("utf-8");
//            // 设置文件名并进行 URL 编码
//            String fileName = URLEncoder.encode(name, "UTF-8").replaceAll("\\+", "%20");
//            response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
//            // 使用 EasyExcel 将数据写入响应的输出流
//            EasyExcelFactory.write(response.getOutputStream())
//                    .head(DynamicRegisterInfoExcelDTO.class)
//                    .sheet(name)
//                    .doWrite(excelDTOS);
//        } catch (IOException e) {
//            log.error("下载excel异常:", e);
//            FileUtil.response2Json(response, "下载excel异常");
//        }
//    }

    public void exportScoreTemplate(HttpServletResponse response, String registerPublishId) {
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(registerPublishId, enterpriseId)) {
                FileUtil.response2Json(response, "缺失必要参数");
                return;
            }

            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneByIdAndEnterpriseId(registerPublishId, enterpriseId);
            if (registerPublishModel == null) {
                FileUtil.response2Json(response, "未查询到报名发布信息");
                return;
            }
            Boolean scoreFlag = registerPublishModel.getScoreFlag();
            if (!Boolean.TRUE.equals(scoreFlag) || !Boolean.TRUE.equals(registerPublishModel.getActivityFlag())) {
                FileUtil.response2Json(response, "未开启笔试成绩查询或未设置笔试禁止操作");
                return;
            }
            List<ActivityInfoModel> activityInfoModels = activityInfoMapper.queryByRegisterPublishId(registerPublishId);
            String title = registerPublishModel.getTitle();
            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            List<DynamicRegisterInfoModel> infoModels = dynamicRegisterInfoMapper.queryBaseInfoByRegisterPublishIdAndStatus(tableName, RegisterInfoStatusEnum.VALID.value());
            // 1. 构建表头
            List<List<String>> headList = new ArrayList<>();
            headList.add(Collections.singletonList("编码"));
            headList.add(Collections.singletonList("姓名"));
            headList.add(Collections.singletonList("证件号码"));
            headList.add(Collections.singletonList("准考证号"));
            // 添加 activity 的 subject 作为动态列
            for (ActivityInfoModel activity : activityInfoModels) {
                String header = String.format("%s\n(保留两位小数，缺考：-1，占比：%s)\n[%s]",
                        activity.getSubject(),
                        activity.getScoreProportion(),
                        activity.getId());
                headList.add(Collections.singletonList(header));
            }
            // 2. 构建数据
            List<List<Object>> dataList = new ArrayList<>();
            for (DynamicRegisterInfoModel infoModel : infoModels) {
                List<Object> row = new ArrayList<>();
                row.add(infoModel.getId());
                row.add(infoModel.getName());
                row.add(infoModel.getIdNumber());
                row.add(infoModel.getAdmissionTicketNumber());
                // 成绩列留空
                for (int i = 0; i < activityInfoModels.size(); i++) {
                    row.add(null); // 或者填默认值，比如 0
                }
                dataList.add(row);
            }
            String name = title + "导入成绩模板";
            // 设置响应的内容类型为 Excel 格式
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 设置文件名并进行 URL 编码
            String fileName = URLEncoder.encode(name, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            // 使用 EasyExcel 将数据写入响应的输出流
            EasyExcelFactory.write(response.getOutputStream())
                    .head(headList)
                    .sheet(name)
                    .doWrite(dataList);
        } catch (Exception e) {
            log.error("下载excel异常:", e);
            FileUtil.response2Json(response, "下载excel异常");
        }
    }


    public I18nResult<Boolean> uploadScoreTemplate(MultipartFile file, String registerPublishId) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try (InputStream inputStream = file.getInputStream()) {  // 使用 try-with-resources 确保流关闭
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(registerPublishId, enterpriseId)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }

            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneByIdAndEnterpriseId(registerPublishId, enterpriseId);
            if (registerPublishModel == null) {
                return result.failedBadRequest().setMessage("未查询到报名发布信息");
            }
            Boolean scoreFlag = registerPublishModel.getScoreFlag();
            if (!Boolean.TRUE.equals(scoreFlag) || !Boolean.TRUE.equals(registerPublishModel.getActivityFlag())) {
                return result.failedBadRequest().setMessage("未开启笔试成绩查询或未设置笔试禁止操作");
            }
            List<ActivityInfoModel> activityInfoModels = activityInfoMapper.queryByRegisterPublishId(registerPublishId);
            Map<String, ActivityInfoModel> activityMap = activityInfoModels.stream()
                    .collect(Collectors.toMap(
                            ActivityInfoModel::getId,
                            Function.identity(),
                            (existing, replacement) -> existing
                    ));
            Set<String> activityIds = activityMap.keySet();
            // 2. 读取 Excel 内容，要求前三行为表头，第四行开始是数据
            List<Map<Integer, String>> excelData = EasyExcel.read(inputStream)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
            if (excelData == null || excelData.size() < 2) {
                return result.failedBadRequest().setMessage("数据不能为空");
            }
            // 3. 读取表头
            Map<Integer, String> headerRowMap = excelData.get(0);
            int fixedColumns = 4; // 前4列是固定列
            // 4. 校验科目ID合法性并建立列号->科目ID映射
            Pattern ID_PATTERN = Pattern.compile("\\[(.*?)\\]$");
            Map<Integer, String> colIndexToActivityId = new HashMap<>();
            for (int colIndex = fixedColumns; colIndex < headerRowMap.size(); colIndex++) {
                String header = String.valueOf(headerRowMap.get(colIndex)).trim();
                Matcher matcher = ID_PATTERN.matcher(header);
                if (!matcher.find()) {
                    return result.failedBadRequest().setMessage("第" + (colIndex + 1) + "列表头缺少科目ID，格式应为 [...]");
                }
                String activityId = matcher.group(1).trim();
                if (!activityIds.contains(activityId)) {
                    return result.failedBadRequest().setMessage("导入文件包含无效科目ID：" + activityId);
                }
                colIndexToActivityId.put(colIndex, activityId);
            }
            if (!Objects.equals(activityIds.size(), colIndexToActivityId.size())) {
                return result.failedBadRequest().setMessage("导入文件场次列数与活动场数不符");
            }
            // 5. 逐行解析数据行（第四行开始）
            List<UpdateScoreDTO> updateScoreDTOS = new ArrayList<>();
            String loginUsername = SecurityUtil.getLoginUsername();
            Long timeMillis = DateUtil.currentTimeMillis();
            for (int rowIndex = 1; rowIndex < excelData.size(); rowIndex++) {
                UpdateScoreDTO updateScoreDTO = new UpdateScoreDTO();
                Map<Integer, String> row = excelData.get(rowIndex);
                if (row.size() < fixedColumns) {
                    return result.failedBadRequest().setMessage("第 " + (rowIndex + 1) + " 行数据列数不足");
                }
                String registerId = row.get(0);
                String name = row.get(1);
                String idNumber = row.get(2);
                String admissionTicketNumber = row.get(3);
                if (StringUtils.isAnyEmpty(registerId, name, idNumber, admissionTicketNumber)) {
                    return result.failedBadRequest().setMessage("第 " + (rowIndex + 1) + " 行数据缺失必要字段");
                }
                registerId = registerId.trim();
                name = name.trim();
                idNumber = idNumber.trim();
                admissionTicketNumber = admissionTicketNumber.trim();
                BigDecimal score = BigDecimal.ZERO;
                Map<String, BigDecimal> activityCompositeScoreMap = new LinkedHashMap<>();
                // 遍历成绩列
                for (Map.Entry<Integer, String> entry : colIndexToActivityId.entrySet()) {
                    int colIndex = entry.getKey();
                    String activityId = entry.getValue();
                    String scoreStr = colIndex < row.size() ? row.get(colIndex) : null;
                    if (StringUtils.isAnyEmpty(scoreStr, activityId)) {
                        return result.failedBadRequest().setMessage("第 " + (rowIndex + 1) + " 行成绩列数据缺失必要字段");
                    }
                    activityId = activityId.trim();
                    scoreStr = scoreStr.trim();
                    if (StringUtils.isAnyEmpty(scoreStr, activityId, registerId, name, idNumber, admissionTicketNumber)) {
                        return result.failedBadRequest().setMessage("第 " + (rowIndex + 1) + " 行数据缺失必要字段");
                    }
                    BigDecimal activityScore = new BigDecimal(scoreStr)
                            .setScale(2, RoundingMode.HALF_UP);
                    activityCompositeScoreMap.put(activityId, activityScore);

                    if (activityScore.compareTo(BigDecimal.ZERO) >= 0) {
                        BigDecimal activityScoreProportion = BigDecimal.ONE;
                        if (activityMap.getOrDefault(activityId, null) != null && activityMap.get(activityId).getScoreProportion() != null) {
                            activityScoreProportion = activityMap.get(activityId).getScoreProportion();
                        }
                        BigDecimal multiply = activityScore.multiply(activityScoreProportion);
                        score = score.add(multiply);
                    }
                }
                score = score.setScale(2, RoundingMode.HALF_UP);
                updateScoreDTO.setRegisterId(registerId);
                updateScoreDTO.setName(name);
                updateScoreDTO.setIdNumber(idNumber);
                updateScoreDTO.setAdmissionTicketNumber(admissionTicketNumber);
                updateScoreDTO.setScore(score);
                updateScoreDTO.setActivityCompositeScoreMap(JsonUtil.object2Json(activityCompositeScoreMap));
                updateScoreDTO.setUpdateBy(loginUsername);
                updateScoreDTO.setUpdateAt(timeMillis);
                updateScoreDTOS.add(updateScoreDTO);
            }

            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            slaveTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(@NonNull TransactionStatus status) {
                    for (UpdateScoreDTO updateScoreDTO : updateScoreDTOS) {
                        dynamicRegisterInfoMapper.updateScoreByUnicode(tableName, updateScoreDTO);
                    }
                }
            });
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("导入成绩发生异常:", e);
            result.failed().setData(false);
        }
        return result;
    }

    public void exportRegisterSign(HttpServletResponse response, String registerPublishId, String spotId) {
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(enterpriseId, registerPublishId, spotId)) {
                FileUtil.response2Json(response, "缺少必要参数");
                return;
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneByIdAndEnterpriseId(registerPublishId, enterpriseId);
            if (registerPublishModel == null) {
                FileUtil.response2Json(response, "未查询到该场次");
                return;
            }
            Boolean allocateSpotFlag = registerPublishModel.getAllocateSpotFlag();
            if (!Boolean.TRUE.equals(allocateSpotFlag)) {
                FileUtil.response2Json(response, "该场次未分配考点，请先分配考场");
                return;
            }
            PublishSpotModel publishSpotModel = publishSpotMapper.queryOneById(spotId);
            if (publishSpotModel == null) {
                FileUtil.response2Json(response, "未查询到该考点");
                return;
            }
            String spot = publishSpotModel.getSpot();
            if (!Objects.equals(enterpriseId, publishSpotModel.getEnterpriseId())) {
                FileUtil.response2Json(response, "没有权限操作");
                return;
            }
            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            List<String> rooms = dynamicRegisterInfoMapper.queryRoomByRegisterPublishIdAndSpotId(tableName, spotId);
            String fileName = URLEncoder.encode(spot + "-考点签到表压缩包.zip", StandardCharsets.UTF_8.name());
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
                for (String room : rooms) {
                    PDDocument document = new PDDocument();
                    List<SignInInfoDTO> allInfos = dynamicRegisterInfoMapper.querySignInInfoDTO(tableName, spotId, room);

                    int pageSize = 12; // 每页 12 条
                    int totalPages = (int) Math.ceil(allInfos.size() / (double) pageSize);

                    for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                        PDPage page = new PDPage(PDRectangle.A4);
                        document.addPage(page);

                        InputStream fontStream = this.getClass().getClassLoader()
                                .getResourceAsStream("static/fonts/simsun.ttf");
                        PDFont font = PDType0Font.load(document, fontStream, true);
                        PDPageContentStream contentStream = new PDPageContentStream(document, page);

                        float margin = 40;
                        float pageWidth = PDRectangle.A4.getWidth();
                        float startX = margin;
                        float startY = PDRectangle.A4.getHeight() - margin;
                        float boxWidth = 240; // 每行两个，所以宽度要调大
                        float boxHeight = 100;
                        float paddingX = 20; // 列间距
                        float paddingY = 25; // 行间距

                        // 页眉：考场号 居中
                        String headerText = "考点： " + spot + "    " + "考场号：" + room;
                        float fontSizeHeader = 12f;
                        float headerTextWidth = font.getStringWidth(headerText) / 1000 * fontSizeHeader;
                        float headerX = (pageWidth - headerTextWidth) / 2;
                        float headerY = PDRectangle.A4.getHeight() - margin + 20;

                        contentStream.beginText();
                        contentStream.setFont(font, fontSizeHeader);
                        contentStream.newLineAtOffset(headerX, headerY);
                        contentStream.showText(headerText);
                        contentStream.endText();

                        // 正文起始 Y 坐标
                        float contentStartY = startY - 20;

                        List<SignInInfoDTO> pageInfos = allInfos.stream()
                                .skip(pageIndex * pageSize)
                                .limit(pageSize)
                                .collect(Collectors.toList());

                        for (int i = 0; i < pageInfos.size(); i++) {
                            int row = i / 2;
                            int col = i % 2;

                            float x = startX + col * (boxWidth + paddingX);
                            float y = contentStartY - row * (boxHeight + paddingY);

                            SignInInfoDTO info = pageInfos.get(i);

                            // 图片
                            try (InputStream imageStream = new URL(minioPictureUrl + info.getPhoto()).openStream()) {
                                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                                byte[] data = new byte[1024];
                                int nRead;
                                while ((nRead = imageStream.read(data, 0, data.length)) != -1) {
                                    buffer.write(data, 0, nRead);
                                }
                                byte[] imageBytes = buffer.toByteArray();
                                PDImageXObject image = PDImageXObject.createFromByteArray(document, imageBytes, "photo");
                                contentStream.drawImage(image, x, y - 60, 60, 60);
                            } catch (Exception e) {
                                log.error("签到表加载图片失败:{} ", info.getPhoto(), e);
                            }

                            // 文字 + 签到签离
                            contentStream.beginText();
                            contentStream.setFont(font, 10);
                            contentStream.newLineAtOffset(x + 65, y - 15);
                            contentStream.showText("姓名: " + info.getName());
                            contentStream.newLineAtOffset(0, -13);
                            contentStream.showText("准考证号: " + info.getAdmissionTicketNumber());
                            contentStream.newLineAtOffset(0, -13);
                            contentStream.showText("身份证号: " + info.getIdNumber());
                            contentStream.newLineAtOffset(0, -13);
                            contentStream.showText("座位号: " + info.getSeatNumber());
                            contentStream.newLineAtOffset(0, -24);
                            contentStream.showText("签到: __________");
                            contentStream.newLineAtOffset(0, -24);
                            contentStream.showText("签离: __________");
                            contentStream.endText();
                        }

                        // 页脚：居中页码
                        String pageNumberText = "第 " + (pageIndex + 1) + " 页，共 " + totalPages + " 页";
                        float fontSizePageNum = 10f;
                        float pageNumberTextWidth = font.getStringWidth(pageNumberText) / 1000 * fontSizePageNum;
                        float pageNumberX = (pageWidth - pageNumberTextWidth) / 2;
                        float pageNumberY = margin - 20;

                        contentStream.beginText();
                        contentStream.setFont(font, fontSizePageNum);
                        contentStream.newLineAtOffset(pageNumberX, pageNumberY);
                        contentStream.showText(pageNumberText);
                        contentStream.endText();

                        contentStream.close();
                    }

                    ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
                    document.save(pdfOut);
                    document.close();

                    ZipEntry entry = new ZipEntry(spot + "_" + room + "号考场签到表.pdf");
                    zipOut.putNextEntry(entry);
                    zipOut.write(pdfOut.toByteArray());
                    zipOut.closeEntry();
                }
            }

        } catch (Exception e) {
            log.error("导出PDF出错:", e);
            FileUtil.response2Json(response, "导出签到表发生异常");
        }
    }

    public void exportRegisterAttachment(HttpServletResponse response, String registerPublishId, RegisterInfoStatusEnum status) {
        try {

            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(enterpriseId, registerPublishId)) {
                FileUtil.response2Json(response, "缺少必要参数");
                return;
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneByIdAndEnterpriseId(registerPublishId, enterpriseId);
            if (registerPublishModel == null) {
                FileUtil.response2Json(response, "未查询到该场次");
                return;
            }

            List<Map<String, Object>> conditions = new ArrayList<>();
            if (status != null) {
                Map<String, Object> condition = new HashMap<>();
                condition.put(KEY, COLUMN_STATUS);
                condition.put(VALUE, status.value());
                condition.put(MATCH_TYPE, MATCH_TYPE_EXACT);
                conditions.add(condition);
            }
            List<String> arrayJsonAttachment = new ArrayList<>();
            String templateCopy = registerPublishModel.getTemplateCopy();
            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            List<String> fields = new ArrayList<>();
            List<String> camelCaseFields = new ArrayList<>();
            String camelCaseName = StringUtil.snakeToCamelCase(COLUMN_NAME);
            String camelCaseCreateBy = StringUtil.snakeToCamelCase(COLUMN_CREATE_BY);
            fields.add(COLUMN_NAME + AS + camelCaseName);
            fields.add(COLUMN_CREATE_BY + AS + camelCaseCreateBy);
            camelCaseFields.add(camelCaseName);
            camelCaseFields.add(camelCaseCreateBy);

            if (StringUtils.isNotEmpty(templateCopy)) {
                ObjectNode templateCopyNode = JsonUtil.parseObjectNode(templateCopy);
                Iterator<Map.Entry<String, JsonNode>> iterator = templateCopyNode.fields();
                while (iterator.hasNext()) {
                    Map.Entry<String, JsonNode> next = iterator.next();
                    JsonNode jsonNode = next.getValue();
                    String column = jsonNode.get(TEMPLATE_KEY).asText();
                    String remark = jsonNode.get(TEMPLATE_REMARK).asText();
                    String formatType = jsonNode.get(TEMPLATE_FORMAT_TYPE).asText();
                    if (FORMAT_TYPE_ARRAY_JSON_ATTACHMENT.equals(formatType)) {
                        String camelCase = StringUtil.snakeToCamelCase(column);
                        fields.add(column + AS + camelCase);
                        arrayJsonAttachment.add(camelCase);
                        camelCaseFields.add(camelCase);
                    }
                }
            }
            if (CollectionUtils.isEmpty(arrayJsonAttachment)) {
                FileUtil.response2Json(response, "附件字段为空");
                return;
            }

            String fileName = URLEncoder.encode(registerPublishModel.getTitle() + "-所有附件压缩包.zip", StandardCharsets.UTF_8.name());
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            List<Map<String, Object>> dynamics = dynamicRegisterInfoMapper.selectDynamic(tableName, fields, conditions);

            // 总ZIP流：写入响应中
            try (ZipOutputStream totalZipOut = new ZipOutputStream(response.getOutputStream())) {
                for (Map<String, Object> dynamic : dynamics) {
                    Object createByObject = dynamic.getOrDefault(StringUtil.snakeToCamelCase(COLUMN_CREATE_BY), "");
                    Object nameObject = dynamic.getOrDefault(StringUtil.snakeToCamelCase(COLUMN_NAME), "");
                    // 临时的用户ZIP输出到内存中
                    try (ByteArrayOutputStream userZipBaos = new ByteArrayOutputStream();
                         ZipOutputStream userZipOut = new ZipOutputStream(userZipBaos)) {
                        for (String attachmentField : arrayJsonAttachment) {
                            Object value = dynamic.getOrDefault(attachmentField, null);
                            if (value != null && StringUtil.isNotEmpty(value.toString())) {
                                ArrayNode arrayNode = JsonUtil.parseArrayNode(value.toString());
                                for (JsonNode jsonNode : arrayNode) {
                                    String name = jsonNode.path(FORMAT_TYPE_ATTACHMENT_NAME).asText("");
                                    String url = jsonNode.path(FORMAT_TYPE_ATTACHMENT_URL).asText("");
                                    String accessUrl = minioFileUrl + url;
                                    try (InputStream fileInput = new URL(accessUrl).openStream()) {
                                        ZipEntry fileEntry = new ZipEntry(name);
                                        userZipOut.putNextEntry(fileEntry);
                                        byte[] buffer = new byte[4096];
                                        int len;
                                        while ((len = fileInput.read(buffer)) != -1) {
                                            userZipOut.write(buffer, 0, len);
                                        }
                                        userZipOut.closeEntry();
                                    } catch (Exception e) {
                                        // 单个文件下载失败可忽略，也可加日志记录
                                        log.error("导出所有附件单个文件下载失败: {}", accessUrl, e);
                                    }
                                }
                            }
                        }
                        userZipOut.finish();
                        // 用户名（或 ID）作为 ZIP 名
                        String userZipName = StringUtil.toSafeString(nameObject) + "_" + StringUtil.toSafeString(createByObject) + ".zip";
                        ZipEntry userZipEntry = new ZipEntry(userZipName);
                        totalZipOut.putNextEntry(userZipEntry);
                        totalZipOut.write(userZipBaos.toByteArray());
                        totalZipOut.closeEntry();
                    } catch (Exception e) {
                        log.error("处理用户zip失败", e);
                    }
                }
                totalZipOut.finish();
            }
        } catch (Exception e) {
            log.error("导出所有附件出错:", e);
            FileUtil.response2Json(response, "导出所有附件发生异常");

        }
    }

    public void exportInterviewTemplate(HttpServletResponse response, String registerPublishId) {
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(registerPublishId, enterpriseId)) {
                FileUtil.response2Json(response, "缺失必要参数");
                return;
            }

            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneByIdAndEnterpriseId(registerPublishId, enterpriseId);
            if (registerPublishModel == null) {
                FileUtil.response2Json(response, "未查询到报名发布信息");
                return;
            }
            String title = registerPublishModel.getTitle();
            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            List<DynamicRegisterInfoModel> infoModels = dynamicRegisterInfoMapper.queryBaseInfoByRegisterPublishIdAndStatus(tableName, RegisterInfoStatusEnum.VALID.value());
            List<InterviewTemplateExcelDTO> excelDTOS = new ArrayList<>();
            for (DynamicRegisterInfoModel infoModel : infoModels) {
                InterviewTemplateExcelDTO excelDTO = new InterviewTemplateExcelDTO();
                excelDTO.setId(infoModel.getId());
                excelDTO.setName(infoModel.getName());
                excelDTO.setIdNumber(infoModel.getIdNumber());
                excelDTO.setAdmissionTicketNumber(infoModel.getAdmissionTicketNumber());
                excelDTOS.add(excelDTO);
            }
            String name = title + "导入面试信息模板";
            // 设置响应的内容类型为 Excel 格式
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 设置文件名并进行 URL 编码
            String fileName = URLEncoder.encode(name, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            // 使用 EasyExcel 将数据写入响应的输出流
            EasyExcelFactory.write(response.getOutputStream())
                    .head(InterviewTemplateExcelDTO.class)
                    .sheet(name)
                    .doWrite(excelDTOS);
        } catch (Exception e) {
            log.error("下载excel异常:", e);
            FileUtil.response2Json(response, "下载excel异常");
        }
    }

    public I18nResult<Boolean> uploadInterviewTemplate(MultipartFile file, String registerPublishId) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try (InputStream inputStream = file.getInputStream()) {  // 使用 try-with-resources 确保流关闭
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(registerPublishId, enterpriseId)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }

            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneByIdAndEnterpriseId(registerPublishId, enterpriseId);
            if (registerPublishModel == null) {
                return result.failedBadRequest().setMessage("未查询到报名发布信息");
            }

            // 使用 EasyExcel 读取上传的 Excel 文件
            List<InterviewTemplateExcelDTO> templateExcelDTOS = EasyExcelFactory.read(inputStream)
                    .head(InterviewTemplateExcelDTO.class)
                    .sheet()
                    .doReadSync();
            String loginUsername = SecurityUtil.getLoginUsername();
            Long timeMillis = DateUtil.currentTimeMillis();
            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            // 遍历读取到的数据
            List<UpdateInterviewInfoDTO> updateInterviewInfoDTOS = new ArrayList<>();
            for (InterviewTemplateExcelDTO templateExcelDTO : templateExcelDTOS) {
                UpdateInterviewInfoDTO updateInterviewInfoDTO = new UpdateInterviewInfoDTO();
                String id = templateExcelDTO.getId();
                String name = templateExcelDTO.getName();
                String idNumber = templateExcelDTO.getIdNumber();
                String admissionTicketNumber = templateExcelDTO.getAdmissionTicketNumber();
                Boolean interviewFlag = templateExcelDTO.getInterviewFlag();
                Date interviewTime = templateExcelDTO.getInterviewTime();
                String interviewSpot = templateExcelDTO.getInterviewSpot();
                String interviewSpotAddress = templateExcelDTO.getInterviewSpotAddress();
                if (StringUtils.isAnyEmpty(id, name, idNumber) || interviewFlag == null) {
                    return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("编号:" + id + "-" + name + "缺失必要参数");
                }
                if (Boolean.TRUE.equals(interviewFlag) &&
                        (interviewTime == null || StringUtils.isAnyEmpty(interviewSpot, interviewSpotAddress))) {
                    return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST)
                            .setMessage("编号:" + id + "-" + name + "面试信息缺失必要参数");
                }
                long timestamp = (interviewTime != null) ? interviewTime.getTime() : 0L;
                updateInterviewInfoDTO.setRegisterId(id);
                updateInterviewInfoDTO.setName(name);
                updateInterviewInfoDTO.setIdNumber(idNumber);
                updateInterviewInfoDTO.setAdmissionTicketNumber(admissionTicketNumber);
                updateInterviewInfoDTO.setInterviewFlag(interviewFlag);
                updateInterviewInfoDTO.setInterviewTime(timestamp);
                updateInterviewInfoDTO.setInterviewSpot(interviewSpot);
                updateInterviewInfoDTO.setInterviewSpotAddress(interviewSpotAddress);
                updateInterviewInfoDTO.setUpdateBy(loginUsername);
                updateInterviewInfoDTO.setUpdateAt(timeMillis);
                updateInterviewInfoDTOS.add(updateInterviewInfoDTO);
            }
            slaveTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(@NonNull TransactionStatus status) {
                    for (UpdateInterviewInfoDTO updateInterviewInfoDTO : updateInterviewInfoDTOS) {
                        dynamicRegisterInfoMapper.updateInterviewInfoByUnicode(tableName, updateInterviewInfoDTO);
                    }
                }
            });
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("导入成绩发生异常:", e);
            result.failed().setData(false);
        }
        return result;
    }

    public void exportInterviewScoreTemplate(HttpServletResponse response, String registerPublishId) {
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(registerPublishId, enterpriseId)) {
                FileUtil.response2Json(response, "缺失必要参数");
                return;
            }

            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneByIdAndEnterpriseId(registerPublishId, enterpriseId);
            if (registerPublishModel == null) {
                FileUtil.response2Json(response, "未查询到报名发布信息");
                return;
            }
            String title = registerPublishModel.getTitle();
            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            List<DynamicRegisterInfoModel> infoModels = dynamicRegisterInfoMapper.queryBaseInfoByRegisterPublishIdAndStatusAndInterviewFlag(tableName, RegisterInfoStatusEnum.VALID.value(), true);
            List<InterviewScoreTemplateExcelDTO> excelDTOS = new ArrayList<>();
            for (DynamicRegisterInfoModel infoModel : infoModels) {
                InterviewScoreTemplateExcelDTO excelDTO = new InterviewScoreTemplateExcelDTO();
                excelDTO.setId(infoModel.getId());
                excelDTO.setName(infoModel.getName());
                excelDTO.setIdNumber(infoModel.getIdNumber());
                excelDTO.setAdmissionTicketNumber(infoModel.getAdmissionTicketNumber());
                excelDTOS.add(excelDTO);
            }
            String name = title + "导入面试成绩模板";
            // 设置响应的内容类型为 Excel 格式
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 设置文件名并进行 URL 编码
            String fileName = URLEncoder.encode(name, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            // 使用 EasyExcel 将数据写入响应的输出流
            EasyExcelFactory.write(response.getOutputStream())
                    .head(InterviewScoreTemplateExcelDTO.class)
                    .sheet(name)
                    .doWrite(excelDTOS);
        } catch (Exception e) {
            log.error("下载excel异常:", e);
            FileUtil.response2Json(response, "下载excel异常");
        }
    }

    public I18nResult<Boolean> uploadInterviewScoreTemplate(MultipartFile file, String registerPublishId) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try (InputStream inputStream = file.getInputStream()) {  // 使用 try-with-resources 确保流关闭
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(registerPublishId, enterpriseId)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }

            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneByIdAndEnterpriseId(registerPublishId, enterpriseId);
            if (registerPublishModel == null) {
                return result.failedBadRequest().setMessage("未查询到报名发布信息");
            }

            // 使用 EasyExcel 读取上传的 Excel 文件
            List<InterviewScoreTemplateExcelDTO> scoreTemplates = EasyExcelFactory.read(inputStream)
                    .head(InterviewScoreTemplateExcelDTO.class)
                    .sheet()
                    .doReadSync();
            String loginUsername = SecurityUtil.getLoginUsername();
            Long timeMillis = DateUtil.currentTimeMillis();
            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            // 遍历读取到的数据
            for (InterviewScoreTemplateExcelDTO scoreTemplate : scoreTemplates) {
                String id = scoreTemplate.getId();
                String name = scoreTemplate.getName();
                String idNumber = scoreTemplate.getIdNumber();
                String interviewScore = scoreTemplate.getInterviewScore();
                if (StringUtils.isAnyEmpty(id, name, idNumber, interviewScore)) {
                    return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("编号:" + id + "-" + name + "缺失必要参数");
                }
            }
            slaveTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(@NonNull TransactionStatus status) {
                    for (InterviewScoreTemplateExcelDTO scoreTemplate : scoreTemplates) {
                        String id = scoreTemplate.getId();
                        String idNumber = scoreTemplate.getIdNumber();
                        String interviewScoreStr = scoreTemplate.getInterviewScore();
                        BigDecimal interviewScore = new BigDecimal(interviewScoreStr)
                                .setScale(2, RoundingMode.HALF_UP);
                        dynamicRegisterInfoMapper.updateInterviewScoreByUnicode(tableName, interviewScore, loginUsername, timeMillis, id, idNumber);
                    }
                }
            });
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("导入成绩发生异常:", e);
            result.failed().setData(false);
        }
        return result;
    }

    public void exportOfferTemplate(HttpServletResponse response, String registerPublishId) {
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(registerPublishId, enterpriseId)) {
                FileUtil.response2Json(response, "缺失必要参数");
                return;
            }

            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneByIdAndEnterpriseId(registerPublishId, enterpriseId);
            if (registerPublishModel == null) {
                FileUtil.response2Json(response, "未查询到报名发布信息");
                return;
            }
            String title = registerPublishModel.getTitle();
            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            List<DynamicRegisterInfoModel> infoModels = dynamicRegisterInfoMapper.queryBaseInfoByRegisterPublishIdAndStatus(tableName, RegisterInfoStatusEnum.VALID.value());
            List<OfferTemplateExcelDTO> excelDTOS = new ArrayList<>();
            for (DynamicRegisterInfoModel infoModel : infoModels) {
                OfferTemplateExcelDTO excelDTO = new OfferTemplateExcelDTO();
                excelDTO.setId(infoModel.getId());
                excelDTO.setName(infoModel.getName());
                excelDTO.setIdNumber(infoModel.getIdNumber());
                excelDTO.setAdmissionTicketNumber(infoModel.getAdmissionTicketNumber());
                excelDTOS.add(excelDTO);
            }
            String name = title + "导入拟录用模板";
            // 设置响应的内容类型为 Excel 格式
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 设置文件名并进行 URL 编码
            String fileName = URLEncoder.encode(name, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            // 使用 EasyExcel 将数据写入响应的输出流
            EasyExcelFactory.write(response.getOutputStream())
                    .head(OfferTemplateExcelDTO.class)
                    .sheet(name)
                    .doWrite(excelDTOS);
        } catch (Exception e) {
            log.error("下载excel异常:", e);
            FileUtil.response2Json(response, "下载excel异常");
        }

    }

    public I18nResult<Boolean> uploadOfferTemplate(MultipartFile file, String registerPublishId) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try (InputStream inputStream = file.getInputStream()) {  // 使用 try-with-resources 确保流关闭
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(registerPublishId, enterpriseId)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }

            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneByIdAndEnterpriseId(registerPublishId, enterpriseId);
            if (registerPublishModel == null) {
                return result.failedBadRequest().setMessage("未查询到报名发布信息");
            }

            // 使用 EasyExcel 读取上传的 Excel 文件
            List<OfferTemplateExcelDTO> offerTemplateExcelDTOS = EasyExcelFactory.read(inputStream)
                    .head(OfferTemplateExcelDTO.class)
                    .sheet()
                    .doReadSync();
            String loginUsername = SecurityUtil.getLoginUsername();
            Long timeMillis = DateUtil.currentTimeMillis();
            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            // 遍历读取到的数据
            for (OfferTemplateExcelDTO excelDTO : offerTemplateExcelDTOS) {
                String id = excelDTO.getId();
                String name = excelDTO.getName();
                String idNumber = excelDTO.getIdNumber();
                Boolean offerFlag = excelDTO.getOfferFlag();
                String offerExplain = excelDTO.getOfferExplain();
                if (StringUtils.isAnyEmpty(id, name, idNumber) || offerFlag == null) {
                    return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("编号:" + id + "-" + name + "缺失必要参数");
                }
            }
            slaveTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(@NonNull TransactionStatus status) {
                    for (OfferTemplateExcelDTO excelDTO : offerTemplateExcelDTOS) {
                        String id = excelDTO.getId();
                        String idNumber = excelDTO.getIdNumber();
                        Boolean offerFlag = excelDTO.getOfferFlag();
                        String offerExplain = excelDTO.getOfferExplain();
                        dynamicRegisterInfoMapper.updateOfferByUnicode(tableName, offerFlag, offerExplain, loginUsername, timeMillis, id, idNumber);
                    }
                }
            });
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("导入成绩发生异常:", e);
            result.failed().setData(false);
        }
        return result;
    }

    public void obtainAdmissionInterviewTicket(HttpServletResponse response, String registerPublishId) {
        try {
            if (StringUtils.isAnyEmpty(registerPublishId)) {
                FileUtil.response2Json(response, "缺失必要参数");
                return;
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneById(registerPublishId);

            if (registerPublishModel == null) {
                FileUtil.response2Json(response, "未查询到考试");
                return;
            }

            if (!Boolean.TRUE.equals(registerPublishModel.getInterviewTicketFlag())) {
                FileUtil.response2Json(response, "未开启打印证件信息");
                return;
            }

            Long timeMillis = DateUtil.currentTimeMillis();
            if (timeMillis < registerPublishModel.getInterviewTicketStartAt() || timeMillis > registerPublishModel.getInterviewTicketEndAt()) {
                FileUtil.response2Json(response, "不在打印证件时间范围内");
                return;
            }

            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            String loginUsername = SecurityUtil.getLoginUsername();
            String templateCopy = registerPublishModel.getTemplateCopy();
            List<String> fields = new ArrayList<>();
            if (StringUtils.isNotEmpty(templateCopy)) {
                ObjectNode templateCopyNode = JsonUtil.parseObjectNode(templateCopy);
                Iterator<Map.Entry<String, JsonNode>> iterator = templateCopyNode.fields();
                while (iterator.hasNext()) {
                    Map.Entry<String, JsonNode> next = iterator.next();
                    String key = next.getKey();
                    JsonNode jsonNode = next.getValue();
                    String column = jsonNode.get(KEY).asText();
                    Boolean keyValue = jsonNode.get(TEMPLATE_KEY_VALUE).asBoolean(false);
                    String remark = jsonNode.get(TEMPLATE_REMARK).asText();
                    fields.add(column + AS + key);
                }
            }

            for (String key : PRESET_DYNAMIC_FIELD_MAP.keySet()) {
                fields.add(key + AS + StringUtil.snakeToCamelCase(key));
            }
            List<Map<String, Object>> conditions = new ArrayList<>();
            Map<String, Object> condition = new HashMap<>();
            condition.put(KEY, COLUMN_CREATE_BY);
            condition.put(VALUE, loginUsername);
            condition.put(MATCH_TYPE, MATCH_TYPE_EXACT);
            conditions.add(condition);
            List<Map<String, Object>> objectNodes = dynamicRegisterInfoMapper.selectDynamic(tableName, fields, conditions);

            if (objectNodes == null || CollectionUtils.isEmpty(objectNodes)) {
                FileUtil.response2Json(response, "未查询到考生信息");
                return;
            }
            Map<String, Object> objectMap = objectNodes.get(0);
            if (!RegisterInfoStatusEnum.VALID.value().equals(objectMap.get(StringUtil.snakeToCamelCase(COLUMN_STATUS)))) {
                FileUtil.response2Json(response, "报名信息无效状态，禁止操作");
                return;
            }
            if (!Boolean.TRUE.equals(objectMap.get(StringUtil.snakeToCamelCase(COLUMN_INTERVIEW_FLAG)))) {
                FileUtil.response2Json(response, "该报名信息未进入面试，禁止操作");
                return;
            }


            InterviewTicketTemplateFlagEnum interviewTicketTemplateFlag = registerPublishModel.getInterviewTicketTemplateFlag();
            InterviewTicketTemplateModel templateModel = interviewTicketTemplateMapper.queryOneByFlag(interviewTicketTemplateFlag);
            String fieldContent = templateModel.getFieldContent();
//            String textContent = templateModel.getTextContent();
            String noticeHtml = registerPublishModel.getInterviewTicketAttach();

            //构造模板引擎
            ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
            //模板所在目录，相对于当前classloader的classpath
            resolver.setPrefix("templates/interview-ticket/");
            //模板文件后缀
            resolver.setSuffix(".html");
            SpringTemplateEngine templateEngine = new SpringTemplateEngine();
            templateEngine.setTemplateResolver(resolver);
            // 预定义报名模板字段取值
            Object name = objectMap.getOrDefault(StringUtil.snakeToCamelCase(COLUMN_NAME), null);
            Object gender = objectMap.getOrDefault(StringUtil.snakeToCamelCase(COLUMN_GENDER), null);
            Object idNumber = objectMap.getOrDefault(StringUtil.snakeToCamelCase(COLUMN_ID_NUMBER), null);
            Object interviewSpot = objectMap.getOrDefault(StringUtil.snakeToCamelCase(COLUMN_INTERVIEW_SPOT), null);
            Object interviewSpotAddress = objectMap.getOrDefault(StringUtil.snakeToCamelCase(COLUMN_INTERVIEW_SPOT_ADDRESS), null);
            Object interviewTime = objectMap.getOrDefault(StringUtil.snakeToCamelCase(COLUMN_INTERVIEW_TIME), null);
            Object photo = objectMap.getOrDefault(StringUtil.snakeToCamelCase(COLUMN_PHOTO), null);

            //构造上下文(Model)
            Context context = new Context();
            String photoUrl = minioPictureUrl + (photo == null ? "" : photo);
            context.setVariable("photoUrl", photoUrl);
            context.setVariable("registerPublishTitle", registerPublishModel.getTitle());
            context.setVariable("name", name);
            context.setVariable("gender", gender);
            context.setVariable("idNumber", idNumber);
            context.setVariable("interviewSpot", interviewSpot);
            context.setVariable("interviewSpotAddress", interviewSpotAddress);
            context.setVariable("interviewTime", interviewTime);
            // 报名模板动态字段
            if (StringUtils.isNotEmpty(fieldContent)) {
                ObjectNode fieldContentNode = JsonUtil.parseObjectNode(fieldContent);
                Iterator<Map.Entry<String, JsonNode>> iterator = fieldContentNode.fields();
                while (iterator.hasNext()) {
                    Map.Entry<String, JsonNode> next = iterator.next();
                    String key = next.getKey();
                    context.setVariable(key, objectMap.getOrDefault(key, null));
                }
            }

            context.setVariable("noticeHtml", noticeHtml);
            //渲染模板
            String example = templateEngine.process(interviewTicketTemplateFlag.name(), context);
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFont(() -> this.getClass().getClassLoader().getResourceAsStream("static/fonts/simsun.ttf"), "simsun");
            builder.useFastMode();
            builder.withHtmlContent(example, ResourceUtils.getURL("classpath:static/images/pdf/").toString());
            response.setContentType("application/pdf");
            String pdfFileName = "准考证.pdf";
            String encodedFileName = URLEncoder.encode(pdfFileName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

            // 获取输出流
            try (OutputStream os = response.getOutputStream()) {
                builder.toStream(os);
                builder.run();
                dynamicRegisterInfoMapper.updateInterviewTicketDownloadCount(tableName, loginUsername);
            } catch (Exception e) {
                log.error("builder.run() 失败:", e);
            }
        } catch (Exception e) {
            log.error("打印证件信息发生异常:", e);
            FileUtil.response2Json(response, "打印证件信息发生异常");
        }
    }
}
