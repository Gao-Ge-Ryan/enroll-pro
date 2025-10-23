package top.gaogle.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.pagehelper.page.PageMethod;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.util.CastUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import top.gaogle.dao.master.ActivityInfoMapper;
import top.gaogle.dao.master.FormTemplateMapper;
import top.gaogle.dao.master.RegisterPublishMapper;
import top.gaogle.dao.slave.DynamicRegisterInfoMapper;
import top.gaogle.framework.config.GaogleConfig;
import top.gaogle.framework.i18n.I18ResultCode;
import top.gaogle.framework.i18n.I18nResult;
import top.gaogle.framework.pojo.PageModel;
import top.gaogle.framework.util.*;
import top.gaogle.pojo.dto.UpdateFinalScoreDTO;
import top.gaogle.pojo.enums.FormTemplateFlagEnum;
import top.gaogle.pojo.enums.HttpStatusEnum;
import top.gaogle.pojo.enums.RegisterInfoApproveEnum;
import top.gaogle.pojo.enums.RegisterInfoStatusEnum;
import top.gaogle.pojo.model.ActivityInfoModel;
import top.gaogle.pojo.model.DynamicRegisterInfoModel;
import top.gaogle.pojo.model.FormTemplateModel;
import top.gaogle.pojo.model.RegisterPublishModel;
import top.gaogle.pojo.param.DynamicRegisterInfoEditParam;
import top.gaogle.pojo.param.DynamicRegisterInfoQueryParam;
import top.gaogle.pojo.param.RegisterUserEditParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static top.gaogle.common.RegisterConst.*;

@Service
public class DynamicRegisterInfoService extends SuperService {
    private final DynamicRegisterInfoMapper dynamicRegisterInfoMapper;
    private final RegisterPublishMapper registerPublishMapper;
    private final TransactionTemplate slaveTransactionTemplate;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final MoreTransactionService moreTransactionService;
    private final ActivityInfoMapper activityInfoMapper;
    private final FormTemplateMapper formTemplateMapper;

    @Autowired
    public DynamicRegisterInfoService(DynamicRegisterInfoMapper dynamicRegisterInfoMapper, RegisterPublishMapper registerPublishMapper, @Qualifier("slaveTransactionTemplate") TransactionTemplate slaveTransactionTemplate, EmailService emailService, TemplateEngine templateEngine, MoreTransactionService moreTransactionService, ActivityInfoMapper activityInfoMapper, FormTemplateMapper formTemplateMapper) {
        this.dynamicRegisterInfoMapper = dynamicRegisterInfoMapper;
        this.registerPublishMapper = registerPublishMapper;
        this.slaveTransactionTemplate = slaveTransactionTemplate;
        this.emailService = emailService;
        this.templateEngine = templateEngine;
        this.moreTransactionService = moreTransactionService;
        this.activityInfoMapper = activityInfoMapper;
        this.formTemplateMapper = formTemplateMapper;
    }

    public I18nResult<Boolean> clientApplyInfo(DynamicRegisterInfoEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String registerPublishId = editParam.getRegisterPublishId();
            ObjectNode objectNode = editParam.getObjectNode();
            if (StringUtils.isAnyEmpty(registerPublishId)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("缺失必要参数");
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneById(registerPublishId);
            if (registerPublishModel == null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("未查询到该场次");
            }
            Long timeMillis = DateUtil.currentTimeMillis();
            Long startAt = registerPublishModel.getStartAt();
            Long endAt = registerPublishModel.getEndAt();
            if (timeMillis < startAt || timeMillis > endAt) {
                return result.failedBadRequest().setMessage("不在报名时间范围内禁止操作");
            }
            String loginUsername = SecurityUtil.getLoginUsername();
            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            DynamicRegisterInfoModel infoModel = dynamicRegisterInfoMapper.queryModelByCreateBy(tableName, loginUsername);
            if (infoModel != null) {
                return result.failedBadRequest().setMessage("已报名，不能重复操作");
            }
            List<String> columns = new ArrayList<>();
            List<Object> values = new ArrayList<>();
            if (objectNode != null && objectNode.size() > 0) {
                String templateCopy = registerPublishModel.getTemplateCopy();
                if (StringUtils.isNotEmpty(templateCopy)) {
                    ObjectNode templateCopyNode = JsonUtil.parseObjectNode(templateCopy);
                    Iterator<Map.Entry<String, JsonNode>> iterator = templateCopyNode.fields();
                    while (iterator.hasNext()) {
                        Map.Entry<String, JsonNode> next = iterator.next();
                        String key = next.getKey();
                        JsonNode jsonNode = next.getValue();
                        String type = jsonNode.get(TEMPLATE_TYPE).asText();
                        String formatType = jsonNode.get(TEMPLATE_FORMAT_TYPE).asText();
                        String column = jsonNode.get(TEMPLATE_KEY).asText();
                        String remark = jsonNode.get(TEMPLATE_REMARK).asText();
                        boolean required = jsonNode.get(TEMPLATE_RULE).get(TEMPLATE_REQUIRED).asBoolean();
                        String regex = jsonNode.get(TEMPLATE_RULE).get(TEMPLATE_REGEX).asText(null);
                        Object columnValue = null;
                        if (objectNode.get(key) != null) {
                            if (Objects.equals(formatType, TEMPLATE_TEXT)) {
                                String tmpValue = objectNode.path(key).asText(null);
                                if (required && StringUtils.isEmpty(tmpValue)) {
                                    return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage(remark + "值缺失");
                                }
                                if (StringUtils.isNotEmpty(regex) && !StringUtil.regexMatches(tmpValue, regex)) {
                                    return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage(remark + "请输入正确格式");
                                }
                                columnValue = tmpValue;
                            } else if (Objects.equals(formatType, TEMPLATE_TIMESTAMP)) {
                                JsonNode node = objectNode.get(key); // 不要用 path
                                Long tmpValue = null;
                                if (node != null && node.isNumber()) {
                                    tmpValue = node.longValue();
                                }
                                if (required && tmpValue == null) {
                                    return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage(remark + "值缺失");
                                }
                                columnValue = tmpValue;
                            } else {
                                columnValue = objectNode.get(key).asText(null);
                            }
                            columns.add(column);
                            values.add(columnValue);
                        }
                    }
                }
            }
            Map<String, String> jsonExtend = new LinkedHashMap<>();

            //预定义字段获取
            String id = editParam.getId();
            String name = editParam.getName();
            String idNumber = editParam.getIdNumber();
            String admissionTicketNumber = editParam.getAdmissionTicketNumber();
            String photo = editParam.getPhoto();
            String phoneNumber = editParam.getPhoneNumber();
            String email = editParam.getEmail();
            String gender = editParam.getGender();
            String educationLevel = editParam.getEducationLevel();
            String graduatedUniversity = editParam.getGraduatedUniversity();
            String major = editParam.getMajor();
            String spotId = editParam.getSpotId();
            String spot = editParam.getSpot();
            String spotAddress = editParam.getSpotAddress();
            String roomNumber = editParam.getRoomNumber();
            String seatNumber = editParam.getSeatNumber();
            String activityCompositeScore = editParam.getActivityCompositeScore();
            BigDecimal score = editParam.getScore();
            BigDecimal interviewScore = editParam.getInterviewScore();
            BigDecimal finalScore = editParam.getFinalScore();
            Boolean interviewFlag = editParam.getInterviewFlag();
            Long interviewTime = editParam.getInterviewTime();
            String interviewSpot = editParam.getInterviewSpot();
            String interviewSpotAddress = editParam.getInterviewSpotAddress();
            Boolean offerFlag = editParam.getOfferFlag();
            String offerExplain = editParam.getOfferExplain();
            Integer ticketDownloadCount = editParam.getTicketDownloadCount();
            Integer interviewTicketDownloadCount = editParam.getInterviewTicketDownloadCount();
            Integer emailSendCount = editParam.getEmailSendCount();
            Integer phoneSendCount = editParam.getPhoneSendCount();
            Integer status = editParam.getStatus();
            Integer approve = editParam.getApprove();
            String reason = editParam.getReason();
//            String jsonExtend = editParam.getJsonExtend();
            String frontendJsonExtend = editParam.getFrontendJsonExtend();
            String createBy = editParam.getCreateBy();
            Long createAt = editParam.getCreateAt();
            String updateBy = editParam.getUpdateBy();
            Long updateAt = editParam.getUpdateAt();

            // 数据库字段填充
            columns.add(COLUMN_ID);
            columns.add(COLUMN_NAME);
            columns.add(COLUMN_ID_NUMBER);
            columns.add(COLUMN_ADMISSION_TICKET_NUMBER);
            columns.add(COLUMN_PHOTO);
            columns.add(COLUMN_PHONE_NUMBER);
            columns.add(COLUMN_EMAIL);
            columns.add(COLUMN_GENDER);
            columns.add(COLUMN_EDUCATION_LEVEL);
            columns.add(COLUMN_GRADUATED_UNIVERSITY);
            columns.add(COLUMN_MAJOR);
//            columns.add(COLUMN_SPOT_ID);
//            columns.add(COLUMN_SPOT);
//            columns.add(COLUMN_SPOT_ADDRESS);
//            columns.add(COLUMN_ROOM_NUMBER);
//            columns.add(COLUMN_SEAT_NUMBER);
//            columns.add(COLUMN_ACTIVITY_COMPOSITE_SCORE);
//            columns.add(COLUMN_SCORE);
//            columns.add(COLUMN_INTERVIEW_SCORE);
//            columns.add(COLUMN_FINAL_SCORE);
            columns.add(COLUMN_INTERVIEW_FLAG);
//            columns.add(COLUMN_INTERVIEW_TIME);
//            columns.add(COLUMN_INTERVIEW_SPOT);
//            columns.add(COLUMN_INTERVIEW_SPOT_ADDRESS);
            columns.add(COLUMN_OFFER_FLAG);
//            columns.add(COLUMN_OFFER_EXPLAIN);
            columns.add(COLUMN_TICKET_DOWNLOAD_COUNT);
            columns.add(COLUMN_INTERVIEW_TICKET_DOWNLOAD_COUNT);
            columns.add(COLUMN_EMAIL_SEND_COUNT);
            columns.add(COLUMN_PHONE_SEND_COUNT);
            columns.add(COLUMN_STATUS);
            columns.add(COLUMN_APPROVE);
//            columns.add(COLUMN_REASON);
            columns.add(COLUMN_JSON_EXTEND);
            columns.add(COLUMN_FRONTEND_JSON_EXTEND);
            columns.add(COLUMN_CREATE_BY);
            columns.add(COLUMN_CREATE_AT);
            columns.add(COLUMN_UPDATE_BY);
            columns.add(COLUMN_UPDATE_AT);
            // 数据库字段值填充
            values.add(UniqueUtil.getUniqueId());
            values.add(name);
            values.add(idNumber);
            values.add(admissionTicketNumber);
            values.add(photo);
            values.add(phoneNumber);
            values.add(email);
            values.add(gender);
            values.add(educationLevel);
            values.add(graduatedUniversity);
            values.add(major);
//            values.add(spotId);
//            values.add(spot);
//            values.add(spotAddress);
//            values.add(roomNumber);
//            values.add(seatNumber);
//            values.add(activityCompositeScore);
//            values.add(score);
//            values.add(interviewScore);
//            values.add(finalScore);
            values.add(false);
//            values.add(interviewTime);
//            values.add(interviewSpot);
//            values.add(interviewSpotAddress);
            values.add(false);
//            values.add(offerExplain);
            values.add(0);
            values.add(0);
            values.add(0);
            values.add(0);
            values.add(RegisterInfoStatusEnum.INIT.value());
            values.add(RegisterInfoApproveEnum.PENDING.value());
//            values.add(reason);
            values.add(JsonUtil.object2Json(jsonExtend));
            values.add(frontendJsonExtend);
            values.add(loginUsername);
            values.add(timeMillis);
            values.add(loginUsername);
            values.add(timeMillis);


            RegisterUserEditParam userEditParam = new RegisterUserEditParam();
            userEditParam.setId(UniqueUtil.getUniqueId());
            userEditParam.setRegisterPublishId(registerPublishId);
            userEditParam.setCreateBy(loginUsername);
            userEditParam.setCreateAt(timeMillis);
            moreTransactionService.clientApplyInfo(tableName, columns, values, userEditParam);
        } catch (Exception e) {
            log.error("客户端报名申请发生异常:", e);
            result.failed().setMessage("客户端报名申请发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> clientUpdateApplyInfo(DynamicRegisterInfoEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String loginUsername = SecurityUtil.getLoginUsername();
            String registerPublishId = editParam.getRegisterPublishId();
            ObjectNode objectNode = editParam.getObjectNode();
            if (StringUtils.isAnyEmpty(registerPublishId)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("缺失必要参数");
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneById(registerPublishId);
            if (registerPublishModel == null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("未查询到该场次");
            }

            Long timeMillis = DateUtil.currentTimeMillis();
            Long startAt = registerPublishModel.getStartAt();
            Long endAt = registerPublishModel.getEndAt();
            if (timeMillis < startAt || timeMillis > endAt) {
                return result.failedBadRequest().setMessage("不在报名时间范围内禁止操作");
            }

            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            DynamicRegisterInfoModel infoModel = dynamicRegisterInfoMapper.queryModelByCreateBy(tableName, loginUsername);
            if (infoModel == null) {
                return result.failedBadRequest().setMessage("未查询到该报名信息");
            }
            Integer modelApprove = infoModel.getApprove();
            if (!Objects.equals(modelApprove, RegisterInfoApproveEnum.EDITABLE.value()) && !Objects.equals(modelApprove, RegisterInfoApproveEnum.REJECTED.value())) {
                return result.failedBadRequest().setMessage("该状态不能修改");
            }
            List<Map<String, Object>> setClauses = new ArrayList<>();
            List<Map<String, Object>> conditions = new ArrayList<>();
            if (objectNode != null && objectNode.size() > 0) {
                String templateCopy = registerPublishModel.getTemplateCopy();
                if (StringUtils.isNotEmpty(templateCopy)) {
                    ObjectNode templateCopyNode = JsonUtil.parseObjectNode(templateCopy);
                    Iterator<Map.Entry<String, JsonNode>> iterator = templateCopyNode.fields();
                    while (iterator.hasNext()) {
                        Map.Entry<String, JsonNode> next = iterator.next();
                        String key = next.getKey();
                        JsonNode jsonNode = next.getValue();
                        String type = jsonNode.get(TEMPLATE_TYPE).asText();
                        String formatType = jsonNode.get(TEMPLATE_FORMAT_TYPE).asText();
                        String column = jsonNode.get(TEMPLATE_KEY).asText();
                        String remark = jsonNode.get(TEMPLATE_REMARK).asText();
                        boolean required = jsonNode.get(TEMPLATE_RULE).get(TEMPLATE_REQUIRED).asBoolean();
                        String regex = jsonNode.get(TEMPLATE_RULE).get(TEMPLATE_REGEX).asText(null);
                        Object columnValue = null;
                        if (objectNode.get(key) != null) {
                            if (Objects.equals(formatType, TEMPLATE_TEXT)) {
                                String tmpValue = objectNode.path(key).asText(null);
                                if (required && StringUtils.isEmpty(tmpValue)) {
                                    return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage(remark + "值缺失");
                                }
                                if (StringUtils.isNotEmpty(regex) && !StringUtil.regexMatches(tmpValue, regex)) {
                                    return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage(remark + "请输入正确格式");
                                }
                                columnValue = tmpValue;
                            } else if (Objects.equals(formatType, TEMPLATE_TIMESTAMP)) {
                                JsonNode node = objectNode.get(key); // 不要用 path
                                Long tmpValue = null;
                                if (node != null && node.isNumber()) {
                                    tmpValue = node.longValue();
                                }
                                if (required && tmpValue == null) {
                                    return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage(remark + "值缺失");
                                }
                                columnValue = tmpValue;
                            } else {
                                columnValue = objectNode.get(key).asText(null);
                            }
                            Map<String, Object> clause = new HashMap<>();
                            clause.put(KEY, column);
                            clause.put(VALUE, columnValue);
                            setClauses.add(clause);
                        }
                    }
                }
            }

            //预定义字段获取
            String id = editParam.getId();
            String name = editParam.getName();
            String idNumber = editParam.getIdNumber();
            String admissionTicketNumber = editParam.getAdmissionTicketNumber();
            String photo = editParam.getPhoto();
            String phoneNumber = editParam.getPhoneNumber();
            String email = editParam.getEmail();
            String gender = editParam.getGender();
            String educationLevel = editParam.getEducationLevel();
            String graduatedUniversity = editParam.getGraduatedUniversity();
            String major = editParam.getMajor();
            String spotId = editParam.getSpotId();
            String spot = editParam.getSpot();
            String spotAddress = editParam.getSpotAddress();
            String roomNumber = editParam.getRoomNumber();
            String seatNumber = editParam.getSeatNumber();
            String activityCompositeScore = editParam.getActivityCompositeScore();
            BigDecimal score = editParam.getScore();
            BigDecimal interviewScore = editParam.getInterviewScore();
            BigDecimal finalScore = editParam.getFinalScore();
            Boolean interviewFlag = editParam.getInterviewFlag();
            Long interviewTime = editParam.getInterviewTime();
            String interviewSpot = editParam.getInterviewSpot();
            String interviewSpotAddress = editParam.getInterviewSpotAddress();
            Boolean offerFlag = editParam.getOfferFlag();
            String offerExplain = editParam.getOfferExplain();
            Integer ticketDownloadCount = editParam.getTicketDownloadCount();
            Integer interviewTicketDownloadCount = editParam.getInterviewTicketDownloadCount();
            Integer emailSendCount = editParam.getEmailSendCount();
            Integer phoneSendCount = editParam.getPhoneSendCount();
            Integer status = editParam.getStatus();
            Integer approve = editParam.getApprove();
            String reason = editParam.getReason();
            String jsonExtend = editParam.getJsonExtend();
            String frontendJsonExtend = editParam.getFrontendJsonExtend();
            String createBy = editParam.getCreateBy();
            Long createAt = editParam.getCreateAt();
            String updateBy = editParam.getUpdateBy();
            Long updateAt = editParam.getUpdateAt();

            //预定义字段值修改
//            if (StringUtils.isNotEmpty(id)) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_ID);
//                clause.put(VALUE, id);
//                setClauses.add(clause);
//            }
            if (StringUtils.isNotEmpty(name)) {
                Map<String, Object> clause = new HashMap<>();
                clause.put(KEY, COLUMN_NAME);
                clause.put(VALUE, name);
                setClauses.add(clause);
            }
            if (StringUtils.isNotEmpty(idNumber)) {
                Map<String, Object> clause = new HashMap<>();
                clause.put(KEY, COLUMN_ID_NUMBER);
                clause.put(VALUE, idNumber);
                setClauses.add(clause);
            }
//            if (StringUtils.isNotEmpty(admissionTicketNumber)) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_ADMISSION_TICKET_NUMBER);
//                clause.put(VALUE, admissionTicketNumber);
//                setClauses.add(clause);
//            }
            if (StringUtils.isNotEmpty(photo)) {
                Map<String, Object> clause = new HashMap<>();
                clause.put(KEY, COLUMN_PHOTO);
                clause.put(VALUE, photo);
                setClauses.add(clause);
            }
            if (StringUtils.isNotEmpty(phoneNumber)) {
                Map<String, Object> clause = new HashMap<>();
                clause.put(KEY, COLUMN_PHONE_NUMBER);
                clause.put(VALUE, phoneNumber);
                setClauses.add(clause);
            }
            if (StringUtils.isNotEmpty(email)) {
                Map<String, Object> clause = new HashMap<>();
                clause.put(KEY, COLUMN_EMAIL);
                clause.put(VALUE, email);
                setClauses.add(clause);
            }
            if (StringUtils.isNotEmpty(gender)) {
                Map<String, Object> clause = new HashMap<>();
                clause.put(KEY, COLUMN_GENDER);
                clause.put(VALUE, gender);
                setClauses.add(clause);
            }
            if (StringUtils.isNotEmpty(educationLevel)) {
                Map<String, Object> clause = new HashMap<>();
                clause.put(KEY, COLUMN_EDUCATION_LEVEL);
                clause.put(VALUE, educationLevel);
                setClauses.add(clause);
            }
            if (StringUtils.isNotEmpty(graduatedUniversity)) {
                Map<String, Object> clause = new HashMap<>();
                clause.put(KEY, COLUMN_GRADUATED_UNIVERSITY);
                clause.put(VALUE, graduatedUniversity);
                setClauses.add(clause);
            }
            if (StringUtils.isNotEmpty(major)) {
                Map<String, Object> clause = new HashMap<>();
                clause.put(KEY, COLUMN_MAJOR);
                clause.put(VALUE, major);
                setClauses.add(clause);
            }
//            if (StringUtils.isNotEmpty(spotId)) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_SPOT_ID);
//                clause.put(VALUE, spotId);
//                setClauses.add(clause);
//            }
//            if (StringUtils.isNotEmpty(spot)) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_SPOT);
//                clause.put(VALUE, spot);
//                setClauses.add(clause);
//            }
//            if (StringUtils.isNotEmpty(spotAddress)) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_SPOT_ADDRESS);
//                clause.put(VALUE, spotAddress);
//                setClauses.add(clause);
//            }
//            if (StringUtils.isNotEmpty(roomNumber)) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_ROOM_NUMBER);
//                clause.put(VALUE, roomNumber);
//                setClauses.add(clause);
//            }
//            if (StringUtils.isNotEmpty(seatNumber)) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_SEAT_NUMBER);
//                clause.put(VALUE, seatNumber);
//                setClauses.add(clause);
//            }
//            if (activityCompositeScore != null) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_ACTIVITY_COMPOSITE_SCORE);
//                clause.put(VALUE, activityCompositeScore);
//                setClauses.add(clause);
//            }
//            if (score != null) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_SCORE);
//                clause.put(VALUE, score);
//                setClauses.add(clause);
//            }
//            if (interviewScore !=null) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_INTERVIEW_SCORE);
//                clause.put(VALUE, interviewScore);
//                setClauses.add(clause);
//            }
//            if (finalScore != null) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_FINAL_SCORE);
//                clause.put(VALUE, finalScore);
//                setClauses.add(clause);
//            }
//            if (interviewFlag != null) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_INTERVIEW_FLAG);
//                clause.put(VALUE, interviewFlag);
//                setClauses.add(clause);
//            }
//            if (interviewTime != null) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_INTERVIEW_TIME);
//                clause.put(VALUE, interviewTime);
//                setClauses.add(clause);
//            }
//            if (StringUtils.isNotEmpty(interviewSpot)) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_INTERVIEW_SPOT);
//                clause.put(VALUE, interviewSpot);
//                setClauses.add(clause);
//            }
//            if (StringUtils.isNotEmpty(interviewSpotAddress)) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_INTERVIEW_SPOT_ADDRESS);
//                clause.put(VALUE, interviewSpotAddress);
//                setClauses.add(clause);
//            }
//            if (offerFlag != null) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_OFFER_FLAG);
//                clause.put(VALUE, offerFlag);
//                setClauses.add(clause);
//            }
//            if (StringUtils.isNotEmpty(offerExplain)) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_OFFER_EXPLAIN);
//                clause.put(VALUE, offerExplain);
//                setClauses.add(clause);
//            }
//            if (ticketDownloadCount != null) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_TICKET_DOWNLOAD_COUNT);
//                clause.put(VALUE, ticketDownloadCount);
//                setClauses.add(clause);
//            }
//            if (interviewTicketDownloadCount != null) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_INTERVIEW_TICKET_DOWNLOAD_COUNT);
//                clause.put(VALUE, interviewTicketDownloadCount);
//                setClauses.add(clause);
//            }
//            if (emailSendCount != null) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_EMAIL_SEND_COUNT);
//                clause.put(VALUE, emailSendCount);
//                setClauses.add(clause);
//            }
//            if (phoneSendCount != null) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_PHONE_SEND_COUNT);
//                clause.put(VALUE, phoneSendCount);
//                setClauses.add(clause);
//            }
//            if (status != null) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_STATUS);
//                clause.put(VALUE, status);
//                setClauses.add(clause);
//            }
//            if (approve != null) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_APPROVE);
//                clause.put(VALUE, approve);
//                setClauses.add(clause);
//            }
//            if (StringUtils.isNotEmpty(reason)) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_REASON);
//                clause.put(VALUE, reason);
//                setClauses.add(clause);
//            }
//            if (StringUtils.isNotEmpty(jsonExtend)) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_JSON_EXTEND);
//                clause.put(VALUE, jsonExtend);
//                setClauses.add(clause);
//            }
            if (StringUtils.isNotEmpty(frontendJsonExtend)) {
                Map<String, Object> clause = new HashMap<>();
                clause.put(KEY, COLUMN_FRONTEND_JSON_EXTEND);
                clause.put(VALUE, frontendJsonExtend);
                setClauses.add(clause);
            }
//            if (StringUtils.isNotEmpty(createBy)) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_CREATE_BY);
//                clause.put(VALUE, createBy);
//                setClauses.add(clause);
//            }
//            if (createAt != null) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_CREATE_AT);
//                clause.put(VALUE, createAt);
//                setClauses.add(clause);
//            }
//            if (StringUtils.isNotEmpty(updateBy)) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_UPDATE_BY);
//                clause.put(VALUE, updateBy);
//                setClauses.add(clause);
//            }
//            if (updateAt != null) {
//                Map<String, Object> clause = new HashMap<>();
//                clause.put(KEY, COLUMN_UPDATE_AT);
//                clause.put(VALUE, updateAt);
//                setClauses.add(clause);
//            }
            Map<String, Object> approveClause = new HashMap<>();
            approveClause.put(KEY, COLUMN_APPROVE);
            approveClause.put(VALUE, RegisterInfoApproveEnum.PENDING.value());
            setClauses.add(approveClause);

            Map<String, Object> reasonClause = new HashMap<>();
            reasonClause.put(KEY, COLUMN_REASON);
            reasonClause.put(VALUE, null);
            setClauses.add(reasonClause);

            Map<String, Object> updateByClause = new HashMap<>();
            updateByClause.put(KEY, COLUMN_UPDATE_BY);
            updateByClause.put(VALUE, loginUsername);
            setClauses.add(updateByClause);

            Map<String, Object> updateAtClause = new HashMap<>();
            updateAtClause.put(KEY, COLUMN_UPDATE_AT);
            updateAtClause.put(VALUE, timeMillis);
            setClauses.add(updateAtClause);


            Map<String, Object> condition = new HashMap<>();
            condition.put(KEY, COLUMN_CREATE_BY);
            condition.put(VALUE, loginUsername);
            conditions.add(condition);
            dynamicRegisterInfoMapper.updateDynamic(tableName, setClauses, conditions);
        } catch (Exception e) {
            log.error("客户端报名申请发生异常:", e);
            result.failed().setMessage("客户端报名申请发生异常");
        }
        return result;
    }


    public I18nResult<Map<String, Object>> clientGetApplyInfo(DynamicRegisterInfoQueryParam queryParam) {
        I18nResult<Map<String, Object>> result = I18nResult.newInstance();
        try {
            String registerPublishId = queryParam.getRegisterPublishId();
            if (StringUtils.isAnyEmpty(registerPublishId)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("缺少必要参数");
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneById(registerPublishId);
            if (registerPublishModel == null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("未查询到该场次");
            }
            String loginUsername = SecurityUtil.getLoginUsername();
            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            List<String> fields = new ArrayList<>();
            List<Map<String, Object>> conditions = new ArrayList<>();
            String templateCopy = registerPublishModel.getTemplateCopy();
            if (StringUtils.isNotEmpty(templateCopy)) {
                ObjectNode templateCopyNode = JsonUtil.parseObjectNode(templateCopy);
                Iterator<Map.Entry<String, JsonNode>> iterator = templateCopyNode.fields();
                while (iterator.hasNext()) {
                    Map.Entry<String, JsonNode> next = iterator.next();
                    String key = next.getKey();
                    JsonNode jsonNode = next.getValue();
                    String column = jsonNode.get(TEMPLATE_KEY).asText();
                    fields.add(column + AS + key);
                }
            }
            for (String key : PRESET_DYNAMIC_FIELD_MAP.keySet()) {
                fields.add(key + AS + StringUtil.snakeToCamelCase(key));
            }
            Map<String, Object> condition = new HashMap<>();
            condition.put(KEY, COLUMN_CREATE_BY);
            condition.put(VALUE, loginUsername);
            condition.put(MATCH_TYPE, MATCH_TYPE_EXACT); // 使用模糊匹配
            conditions.add(condition);
            List<Map<String, Object>> objectNodes = dynamicRegisterInfoMapper.selectDynamic(tableName, fields, conditions);
            if (CollectionUtils.isEmpty(objectNodes)) {
                return result.succeed().setMessage(null);
            } else {
                Map<String, Object> objectMap = objectNodes.get(0);
                objectMap.put("template_flag", registerPublishModel.getTemplateFlag());
                result.succeed().setData(objectMap);
            }
        } catch (Exception e) {
            log.error("查询个人报名发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "查询个人报名发生异常");
        }
        return result;
    }

    public I18nResult<DynamicRegisterInfoModel> clientGetScore(DynamicRegisterInfoQueryParam queryParam) {
        I18nResult<DynamicRegisterInfoModel> result = I18nResult.newInstance();
        try {
            String registerPublishId = queryParam.getRegisterPublishId();
            if (StringUtils.isAnyEmpty(registerPublishId)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("缺少必要参数");
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneById(registerPublishId);
            if (registerPublishModel == null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("未查询到该场次");
            }

            if (!Boolean.TRUE.equals(registerPublishModel.getScoreFlag())) {
                return result.failedBadRequest().setMessage("该考试未开启成绩查询");
            }
            Long scoreStartAt = registerPublishModel.getScoreStartAt();
            Long scoreEndAt = registerPublishModel.getScoreEndAt();
            Long timeMillis = DateUtil.currentTimeMillis();
            if (timeMillis < scoreStartAt || timeMillis > scoreEndAt) {
                return result.failedBadRequest().setMessage("不在成绩查询时间范围内");
            }
            List<ActivityInfoModel> activityInfoModels = activityInfoMapper.queryByRegisterPublishId(registerPublishId);
            String loginUsername = SecurityUtil.getLoginUsername();
            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            DynamicRegisterInfoModel registerInfoModel = dynamicRegisterInfoMapper.queryScoreByCreateBy(tableName, loginUsername);
            if (registerInfoModel == null || registerInfoModel.getScore() == null || StringUtils.isEmpty(registerInfoModel.getActivityCompositeScore())) {
                return result.succeed().setData(null);
            } else {
                String activityCompositeScore = registerInfoModel.getActivityCompositeScore();
                Map<String, BigDecimal> activityCompositeScoreMap = CastUtils.cast(Objects.requireNonNull(JsonUtil.json2Object(activityCompositeScore, Map.class)));
                for (ActivityInfoModel activityInfoModel : activityInfoModels) {
                    activityInfoModel.setActivityScore(activityCompositeScoreMap.getOrDefault(activityInfoModel.getId(), null));
                }
                registerInfoModel.setActivityInfoModels(activityInfoModels);
                return result.succeed().setData(registerInfoModel);
            }
        } catch (Exception e) {
            log.error("查询发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "查询发生异常");
        }
        return result;
    }

    public I18nResult<PageModel<Map<String, Object>>> queryByPageAndCondition(DynamicRegisterInfoQueryParam queryParam) {
        I18nResult<PageModel<Map<String, Object>>> result = I18nResult.newInstance();
        try {
            String registerPublishId = queryParam.getRegisterPublishId();
            Integer approve = queryParam.getApprove();
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(registerPublishId, enterpriseId)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("缺少必要参数");
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneByIdAndEnterpriseId(registerPublishId, enterpriseId);
            if (registerPublishModel == null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("未查询到该场次");
            }
            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            List<String> fields = new ArrayList<>();
            List<Map<String, Object>> conditions = new ArrayList<>();
            Map<String, String> keyValueName = new LinkedHashMap<>();
            String templateCopy = registerPublishModel.getTemplateCopy();
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
                    if (Boolean.TRUE.equals(keyValue)) {
                        keyValueName.put(key, remark);
                    }
                }
            }

            for (String key : PRESET_DYNAMIC_FIELD_MAP.keySet()) {
                fields.add(key + AS + StringUtil.snakeToCamelCase(key));
            }
            PageMethod.startPage(queryParam.getPageNum(), queryParam.getPageSize());
            if (approve != null) {
                Map<String, Object> condition = new HashMap<>();
                condition.put(KEY, COLUMN_APPROVE);
                condition.put(VALUE, approve);
                condition.put(MATCH_TYPE, MATCH_TYPE_EXACT); // 使用匹配
                conditions.add(condition);
            }
            List<Map<String, Object>> objectNodes = dynamicRegisterInfoMapper.selectDynamicByQueryParam(tableName, fields, conditions, queryParam);
            for (Map<String, Object> objectNode : objectNodes) {
                objectNode.put("templateFlag", registerPublishModel.getTemplateFlag());
                List<String> keyValueList = new ArrayList<>();
                for (Map.Entry<String, String> entry : keyValueName.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    Object obj = objectNode.getOrDefault(key, "");
                    keyValueList.add(value + "：" + (obj == null ? "" : obj));
                }
                objectNode.put(TEMPLATE_KEY_VALUE, String.join(";", keyValueList));
            }
            PageModel<Map<String, Object>> pageModel = new PageModel<>(objectNodes);
            result.succeed().setData(pageModel);
        } catch (Exception e) {
            log.error("查询个人报名发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "查询个人报名发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> approve(DynamicRegisterInfoEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String registerPublishId = editParam.getRegisterPublishId();
            String id = editParam.getId();
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(registerPublishId, id)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("缺少必要参数");
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneByIdAndEnterpriseId(registerPublishId, enterpriseId);
            if (registerPublishModel == null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("未查询到该场次");
            }
            Long timeMillis = DateUtil.currentTimeMillis();
            Long startAt = registerPublishModel.getStartAt();
            Long endAt = registerPublishModel.getEndAt();
            String title = registerPublishModel.getTitle();
            if (timeMillis < startAt || timeMillis > endAt) {
                return result.failedBadRequest().setMessage("不在报名时间范围内禁止操作");
            }
            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            DynamicRegisterInfoModel infoModel = dynamicRegisterInfoMapper.queryModelById(tableName, id);

            if (infoModel == null) {
                return result.failedBadRequest().setMessage("未查询到报名信息");
            }
            if (!Objects.equals(infoModel.getApprove(), RegisterInfoApproveEnum.PENDING.value())) {
                return result.failedBadRequest().setMessage("不是待审核状态不能操作");
            }
            String createBy = infoModel.getCreateBy();
            slaveTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(@NonNull TransactionStatus status) {

                    List<Map<String, Object>> setClauses = new ArrayList<>();
                    Map<String, Object> clause = new HashMap<>();
                    clause.put(KEY, COLUMN_APPROVE);
                    clause.put(VALUE, RegisterInfoApproveEnum.APPROVED.value());
                    setClauses.add(clause);
                    List<Map<String, Object>> conditions = new ArrayList<>();
                    HashMap<String, Object> condition = new HashMap<>();
                    condition.put(KEY, COLUMN_ID);
                    condition.put(VALUE, id);
                    conditions.add(condition);
                    dynamicRegisterInfoMapper.updateDynamic(tableName, setClauses, conditions);

                    String endTitle = title + " 报名审核通过";
                    String endMessage = "报名信息已审核通过";
                    if (Boolean.FALSE.equals(registerPublishModel.getPayFlag())) {
                        dynamicRegisterInfoMapper.updateStatusById(tableName, id, RegisterInfoStatusEnum.VALID.value());
                    } else {
                        endTitle = endTitle + "，请及时缴费！";
                        endMessage = endMessage + "，请及时缴费";
                    }
                    String noticeTemplate = "<p style='font-family: Arial, sans-serif; font-size: 16px; color: #333;'>"
                            + "【<a href='#{clientUrl}' target='_blank' style='text-decoration: none; font-weight: bold; color: #3399FF;'>#{systemName}</a>】您的#{title}（编号：#{registerPublishId}"
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
                    emailService.sendHTML(createBy, endTitle, content);
                }
            });
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("审批发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "审批发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> approveNot(DynamicRegisterInfoEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String registerPublishId = editParam.getRegisterPublishId();
            String id = editParam.getId();
            String reason = editParam.getReason();
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(registerPublishId, id, reason)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("缺少必要参数");
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneByIdAndEnterpriseId(registerPublishId, enterpriseId);
            if (registerPublishModel == null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("未查询到该场次");
            }
            Long timeMillis = DateUtil.currentTimeMillis();
            Long startAt = registerPublishModel.getStartAt();
            Long endAt = registerPublishModel.getEndAt();
            if (timeMillis < startAt || timeMillis > endAt) {
                return result.failedBadRequest().setMessage("不在报名时间范围内禁止操作");
            }
            String title = registerPublishModel.getTitle();
            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;

            DynamicRegisterInfoModel infoModel = dynamicRegisterInfoMapper.queryModelById(tableName, id);

            if (infoModel == null) {
                return result.failedBadRequest().setMessage("未查询到报名信息");
            }
            if (!Objects.equals(infoModel.getApprove(), RegisterInfoApproveEnum.PENDING.value())) {
                return result.failedBadRequest().setMessage("不是待审核状态不能操作");
            }
            String createBy = infoModel.getCreateBy();
            List<Map<String, Object>> setClauses = new ArrayList<>();
            Map<String, Object> statusClause = new HashMap<>();
            statusClause.put(KEY, COLUMN_APPROVE);
            statusClause.put(VALUE, RegisterInfoApproveEnum.REJECTED.value());
            setClauses.add(statusClause);
            Map<String, Object> reasonClause = new HashMap<>();
            reasonClause.put(KEY, COLUMN_REASON);
            reasonClause.put(VALUE, reason);
            setClauses.add(reasonClause);
            List<Map<String, Object>> conditions = new ArrayList<>();
            HashMap<String, Object> condition = new HashMap<>();
            condition.put(KEY, COLUMN_ID);
            condition.put(VALUE, id);
            conditions.add(condition);
            slaveTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(@NonNull TransactionStatus status) {
                    dynamicRegisterInfoMapper.updateDynamic(tableName, setClauses, conditions);
                    String endTitle = title + " 报名审核未通过";
                    String noticeTemplate = "<p style='font-family: Arial, sans-serif; font-size: 16px; color: #333;'>"
                            + "【<a href='#{clientUrl}' target='_blank' style='text-decoration: none; font-weight: bold; color: #3399FF;'>#{systemName}</a>】您的#{title}（编号：#{registerPublishId}"
                            + "）报名信息审核不通过，原因如下：<span style='font-weight: bold; color: #FF5733; font-size: 17px;'>#{endMessage}</span>，请您在报名时间范围内及时修改信息以完成审核，更多信息请访问官方网址！</p>"
                            + "<div style='margin-top: 15px; text-align: center;'>"
                            + "<a href='#{clientUrl}' target='_blank' "
                            + "style='display: inline-block; padding: 10px 20px; background-color: rgba(0, 123, 255, 0.1); color: #336699; font-weight: 500; "
                            + "border-radius: 5px; text-decoration: none; border: 1px solid rgba(0,123,255,0.3);'>访问官网</a></div>";
                    String noticeHtml = noticeTemplate.replace("#{systemName}", GaogleConfig.getSystemName()).replace("#{title}", title)
                            .replace("#{registerPublishId}", registerPublishId).replace("#{endMessage}", reason).replace("#{clientUrl}", GaogleConfig.getClientUrl());
                    Context context = new Context();
                    context.setVariable("title", endTitle);
                    context.setVariable("noticeHtml", noticeHtml);
                    String content = templateEngine.process(GENERAL_HTML, context);
                    emailService.sendHTML(createBy, endTitle, content);
                }
            });
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("审批发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "审批发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> approveRevocation(DynamicRegisterInfoEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String registerPublishId = editParam.getRegisterPublishId();
            String loginUsername = SecurityUtil.getLoginUsername();
            if (StringUtils.isAnyEmpty(registerPublishId)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("缺少必要参数");
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneById(registerPublishId);
            if (registerPublishModel == null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("未查询到该场次");
            }

            Long timeMillis = DateUtil.currentTimeMillis();
            Long startAt = registerPublishModel.getStartAt();
            Long endAt = registerPublishModel.getEndAt();
            if (timeMillis < startAt || timeMillis > endAt) {
                return result.failedBadRequest().setMessage("不在报名时间范围内禁止操作");
            }

            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;

            DynamicRegisterInfoModel infoModel = dynamicRegisterInfoMapper.queryModelByCreateBy(tableName, loginUsername);
            if (infoModel == null) {
                return result.failedBadRequest().setMessage("未查询到报名信息");
            }
            if (!Objects.equals(infoModel.getApprove(), RegisterInfoApproveEnum.PENDING.value())) {
                return result.failedBadRequest().setMessage("不是待审核状态不能操作");
            }
            List<Map<String, Object>> setClauses = new ArrayList<>();
            Map<String, Object> statusClause = new HashMap<>();
            statusClause.put(KEY, COLUMN_APPROVE);
            statusClause.put(VALUE, RegisterInfoApproveEnum.EDITABLE.value());
            setClauses.add(statusClause);
            List<Map<String, Object>> conditions = new ArrayList<>();
            HashMap<String, Object> condition = new HashMap<>();
            condition.put(KEY, COLUMN_CREATE_BY);
            condition.put(VALUE, loginUsername);
            conditions.add(condition);
            dynamicRegisterInfoMapper.updateDynamic(tableName, setClauses, conditions);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("撤回审批发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "撤回审批发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> inputScore(DynamicRegisterInfoEditParam editParam) {

        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String registerPublishId = editParam.getRegisterPublishId();
            String id = editParam.getId();
            Map<String, BigDecimal> activityCompositeScoreMap = editParam.getActivityCompositeScoreMap();
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(registerPublishId, id, enterpriseId) || CollectionUtils.isEmpty(activityCompositeScoreMap)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("缺少必要参数");
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneByIdAndEnterpriseId(registerPublishId, enterpriseId);
            if (registerPublishModel == null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("未查询到该场次");
            }
            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            DynamicRegisterInfoModel dynamicRegisterInfoModel = dynamicRegisterInfoMapper.queryModelById(tableName, id);
            if (!Objects.equals(RegisterInfoStatusEnum.VALID.value(), dynamicRegisterInfoModel.getStatus())) {
                return result.failedBadRequest().setMessage("该报名信息无效状态，禁止操作");
            }
            List<ActivityInfoModel> activityInfoModels = activityInfoMapper.queryByRegisterPublishId(registerPublishId);
            Map<String, ActivityInfoModel> activityMap = activityInfoModels.stream()
                    .collect(Collectors.toMap(
                            ActivityInfoModel::getId,
                            Function.identity(),
                            (existing, replacement) -> existing
                    ));
            Set<String> activityIds = activityMap.keySet();
            if (!Objects.equals(activityIds.size(), activityCompositeScoreMap.size())) {
                return result.failedBadRequest().setMessage("场次数量有误");
            }
            BigDecimal score = BigDecimal.ZERO;
            for (Map.Entry<String, BigDecimal> entry : activityCompositeScoreMap.entrySet()) {
                String activityId = entry.getKey();
                BigDecimal activityScore = entry.getValue();
                if (!activityIds.contains(activityId)) {
                    return result.failedBadRequest().setMessage(activityId + "场次不存在");
                }
                if (activityScore == null) {
                    return result.failedBadRequest().setMessage(activityId + "分数为空");
                }
                activityScore = activityScore.setScale(2, RoundingMode.HALF_UP);
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
            List<Map<String, Object>> setClauses = new ArrayList<>();
            Map<String, Object> scoreClause = new HashMap<>();
            scoreClause.put(KEY, COLUMN_SCORE);
            scoreClause.put(VALUE, score);
            setClauses.add(scoreClause);
            Map<String, Object> activityCompositeScoreMapClause = new HashMap<>();
            activityCompositeScoreMapClause.put(KEY, COLUMN_ACTIVITY_COMPOSITE_SCORE);
            activityCompositeScoreMapClause.put(VALUE, JsonUtil.object2Json(activityCompositeScoreMap));
            setClauses.add(activityCompositeScoreMapClause);
            List<Map<String, Object>> conditions = new ArrayList<>();
            HashMap<String, Object> condition = new HashMap<>();
            condition.put(KEY, COLUMN_ID);
            condition.put(VALUE, id);
            conditions.add(condition);
            dynamicRegisterInfoMapper.updateDynamic(tableName, setClauses, conditions);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("录入笔试成绩发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "录入笔试成绩异常");
        }
        return result;
    }

    public I18nResult<Boolean> inputInterviewInfo(DynamicRegisterInfoEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String registerPublishId = editParam.getRegisterPublishId();
            String id = editParam.getId();
            String enterpriseId = SecurityUtil.getEnterpriseId();
            Boolean interviewFlag = editParam.getInterviewFlag();
            String interviewSpot = editParam.getInterviewSpot();
            Long interviewTime = editParam.getInterviewTime();
            String interviewSpotAddress = editParam.getInterviewSpotAddress();
            if (StringUtils.isAnyEmpty(registerPublishId, id, enterpriseId) || interviewFlag == null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("缺少必要参数");
            }
            if (Boolean.TRUE.equals(interviewFlag) && (StringUtils.isEmpty(interviewSpot) || interviewTime == null || StringUtils.isEmpty(interviewSpotAddress))) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("面试信息缺失必要参数");
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneByIdAndEnterpriseId(registerPublishId, enterpriseId);
            if (registerPublishModel == null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("未查询到该场次");
            }
            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            DynamicRegisterInfoModel dynamicRegisterInfoModel = dynamicRegisterInfoMapper.queryModelById(tableName, id);
            if (!Objects.equals(RegisterInfoStatusEnum.VALID.value(), dynamicRegisterInfoModel.getStatus())) {
                return result.failedBadRequest().setMessage("该报名信息无效状态，禁止操作");
            }
            List<Map<String, Object>> setClauses = new ArrayList<>();
            Map<String, Object> interviewFlagClause = new HashMap<>();
            interviewFlagClause.put(KEY, COLUMN_INTERVIEW_FLAG);
            interviewFlagClause.put(VALUE, interviewFlag);
            setClauses.add(interviewFlagClause);
            Map<String, Object> interviewTimeClause = new HashMap<>();
            interviewTimeClause.put(KEY, COLUMN_INTERVIEW_TIME);
            interviewTimeClause.put(VALUE, interviewTime);
            setClauses.add(interviewTimeClause);
            Map<String, Object> interviewSpotClause = new HashMap<>();
            interviewSpotClause.put(KEY, COLUMN_INTERVIEW_SPOT);
            interviewSpotClause.put(VALUE, interviewSpot);
            setClauses.add(interviewSpotClause);
            Map<String, Object> interviewSpotAddressClause = new HashMap<>();
            interviewSpotAddressClause.put(KEY, COLUMN_INTERVIEW_SPOT_ADDRESS);
            interviewSpotAddressClause.put(VALUE, interviewSpotAddress);
            setClauses.add(interviewSpotAddressClause);


            List<Map<String, Object>> conditions = new ArrayList<>();
            HashMap<String, Object> condition = new HashMap<>();
            condition.put(KEY, COLUMN_ID);
            condition.put(VALUE, id);
            conditions.add(condition);
            dynamicRegisterInfoMapper.updateDynamic(tableName, setClauses, conditions);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("录入笔试成绩发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "录入笔试成绩异常");
        }
        return result;
    }

    public I18nResult<Boolean> inputInterviewScore(DynamicRegisterInfoEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String registerPublishId = editParam.getRegisterPublishId();
            String id = editParam.getId();
            BigDecimal interviewScore = editParam.getInterviewScore();
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(registerPublishId, id, enterpriseId) || interviewScore == null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("缺少必要参数");
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneByIdAndEnterpriseId(registerPublishId, enterpriseId);
            if (registerPublishModel == null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("未查询到该场次");
            }
            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            DynamicRegisterInfoModel dynamicRegisterInfoModel = dynamicRegisterInfoMapper.queryModelById(tableName, id);
            if (!Objects.equals(RegisterInfoStatusEnum.VALID.value(), dynamicRegisterInfoModel.getStatus())) {
                return result.failedBadRequest().setMessage("该报名信息无效状态，禁止操作");
            }
            if (!Boolean.TRUE.equals(dynamicRegisterInfoModel.getInterviewFlag())) {
                return result.failedBadRequest().setMessage("该报名信息未面试，禁止操作");
            }
            interviewScore = interviewScore.setScale(2, RoundingMode.HALF_UP);
            List<Map<String, Object>> setClauses = new ArrayList<>();
            Map<String, Object> interviewScoreClause = new HashMap<>();
            interviewScoreClause.put(KEY, COLUMN_INTERVIEW_SCORE);
            interviewScoreClause.put(VALUE, interviewScore);
            setClauses.add(interviewScoreClause);

            List<Map<String, Object>> conditions = new ArrayList<>();
            HashMap<String, Object> condition = new HashMap<>();
            condition.put(KEY, COLUMN_ID);
            condition.put(VALUE, id);
            conditions.add(condition);
            dynamicRegisterInfoMapper.updateDynamic(tableName, setClauses, conditions);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("录入笔试成绩发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "录入笔试成绩异常");
        }
        return result;
    }

    public I18nResult<Boolean> calculateAllFinalScore(String registerPublishId) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            String loginUsername = SecurityUtil.getLoginUsername();
            if (StringUtils.isAnyEmpty(registerPublishId, enterpriseId)) {
                return result.failedBadRequest().setMessage("缺少必要参数");
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneByIdAndEnterpriseId(registerPublishId, enterpriseId);
            if (registerPublishModel == null) {
                return result.failedBadRequest().setMessage("未查询到该场次");
            }
            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            List<DynamicRegisterInfoModel> dynamicRegisterInfoModels = dynamicRegisterInfoMapper.queryBaseInfoByRegisterPublishIdAndStatus(tableName, RegisterInfoStatusEnum.VALID.value());
            if (!CollectionUtils.isEmpty(dynamicRegisterInfoModels)) {
                BigDecimal scoreProportion = registerPublishModel.getScoreProportion();
                BigDecimal interviewScoreProportion = registerPublishModel.getInterviewScoreProportion();
                List<UpdateFinalScoreDTO> updateFinalScoreDTOS = new ArrayList<>();
                Long timeMillis = DateUtil.currentTimeMillis();
                for (DynamicRegisterInfoModel dynamicRegisterInfoModel : dynamicRegisterInfoModels) {
                    String id = dynamicRegisterInfoModel.getId();
                    BigDecimal score = dynamicRegisterInfoModel.getScore();
                    BigDecimal interviewScore = dynamicRegisterInfoModel.getInterviewScore();
                    if (score == null || interviewScore == null) {
                        BigDecimal finalScore = BigDecimal.ZERO;
                        if (score != null && score.compareTo(BigDecimal.ZERO) >= 0) {
                            finalScore = finalScore.add(score.multiply(scoreProportion));
                        }
                        if (interviewScore != null && interviewScore.compareTo(BigDecimal.ZERO) >= 0) {
                            finalScore = finalScore.add(interviewScore.multiply(interviewScoreProportion));
                        }
                        finalScore = finalScore.setScale(2, RoundingMode.HALF_UP);
                        UpdateFinalScoreDTO updateFinalScoreDTO = new UpdateFinalScoreDTO();
                        updateFinalScoreDTO.setId(id);
                        updateFinalScoreDTO.setFinalScore(finalScore);
                        updateFinalScoreDTO.setUpdateBy(loginUsername);
                        updateFinalScoreDTO.setUpdateAt(timeMillis);
                        updateFinalScoreDTOS.add(updateFinalScoreDTO);
                    }
                }
                if (!CollectionUtils.isEmpty(updateFinalScoreDTOS)) {
                    slaveTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
                        @Override
                        protected void doInTransactionWithoutResult(@NonNull TransactionStatus status) {
                            for (UpdateFinalScoreDTO updateFinalScoreDTO : updateFinalScoreDTOS) {
                                dynamicRegisterInfoMapper.updateFinalScoreByUnicode(tableName, updateFinalScoreDTO);
                            }
                        }
                    });
                }
            }
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("计算所有人最终成绩发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "计算所有人最终成绩异常");
        }
        return result;
    }

    public I18nResult<List<DynamicRegisterInfoModel>> clientGetOfferShow(DynamicRegisterInfoQueryParam queryParam) {
        I18nResult<List<DynamicRegisterInfoModel>> result = I18nResult.newInstance();
        try {
            String registerPublishId = queryParam.getRegisterPublishId();
            if (StringUtils.isAnyEmpty(registerPublishId)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("缺少必要参数");
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneById(registerPublishId);
            if (registerPublishModel == null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("未查询到该场次");
            }

            if (!Boolean.TRUE.equals(registerPublishModel.getOfferStatusShowFlag())) {
                return result.failedBadRequest().setMessage("暂未公布");
            }
            FormTemplateFlagEnum templateFlag = registerPublishModel.getTemplateFlag();

            FormTemplateModel formTemplateModel = formTemplateMapper.queryOneByFlag(templateFlag);
            if (formTemplateModel == null) {
                return result.failedBadRequest().setMessage("未查询到表单模板信息");
            }
            String offerContent = formTemplateModel.getOfferContent();
            List<String> sortOrders = new ArrayList<>();
            if (StringUtils.isNotEmpty(offerContent)) {
                ObjectNode templateCopyNode = JsonUtil.parseObjectNode(offerContent);
                Iterator<Map.Entry<String, JsonNode>> iterator = templateCopyNode.fields();
                while (iterator.hasNext()) {
                    Map.Entry<String, JsonNode> next = iterator.next();
                    String key = next.getKey();
                    JsonNode jsonNode = next.getValue();
                    String sort = jsonNode.get(TEMPLATE_SORT).asText();
                    String order = jsonNode.get(TEMPLATE_ORDER).asText();
                    sortOrders.add(sort + " " + order);

                }
            }
            sortOrders.add("create_at asc");
            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            List<DynamicRegisterInfoModel> registerInfoModels = dynamicRegisterInfoMapper.queryBaseInfoByOfferFlagAndStatus(tableName, true, RegisterInfoStatusEnum.VALID.value(), queryParam.getSearch(), String.join(",", sortOrders));
            registerInfoModels.forEach(r -> r.setIdNumber(StringUtil.maskIdNumber(r.getIdNumber())));
            result.succeed().setData(registerInfoModels);
        } catch (Exception e) {
            log.error("查询发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "查询发生异常");
        }
        return result;
    }
}
