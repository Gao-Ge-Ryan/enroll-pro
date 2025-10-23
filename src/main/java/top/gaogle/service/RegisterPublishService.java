package top.gaogle.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.pagehelper.page.PageMethod;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import top.gaogle.dao.master.*;
import top.gaogle.dao.slave.DynamicRegisterInfoMapper;
import top.gaogle.framework.config.GaogleConfig;
import top.gaogle.framework.i18n.I18ResultCode;
import top.gaogle.framework.i18n.I18nResult;
import top.gaogle.framework.pojo.PageModel;
import top.gaogle.framework.util.*;
import top.gaogle.pojo.dto.AllocateSpotDTO;
import top.gaogle.pojo.dto.OfferPutDTO;
import top.gaogle.pojo.dto.UserRegisterDTO;
import top.gaogle.pojo.enums.*;
import top.gaogle.pojo.model.*;
import top.gaogle.pojo.param.ActivityInfoEditParam;
import top.gaogle.pojo.param.RegisterPublishEditParam;
import top.gaogle.pojo.param.RegisterPublishQueryParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static top.gaogle.common.RegisterConst.*;

@Service
public class RegisterPublishService extends SuperService {
    private final RegisterPublishMapper registerPublishMapper;
    private final FormTemplateMapper formTemplateMapper;
    private final TransactionTemplate transactionTemplate;
    private final PublishSpotMapper publishSpotMapper;
    private final TransactionTemplate slaveTransactionTemplate;
    private final DynamicRegisterInfoMapper dynamicRegisterInfoMapper;
    private final MoreTransactionService moreTransactionService;
    private final EnterpriseMapper enterpriseMapper;
    private final SysJobService sysJobService;
    private final SpotInfoMapper spotInfoMapper;
    private final ActivityInfoMapper activityInfoMapper;
    private final TicketTemplateMapper ticketTemplateMapper;
    private final InterviewTicketTemplateMapper interviewTicketTemplateMapper;


    @Autowired
    public RegisterPublishService(RegisterPublishMapper registerPublishMapper, FormTemplateMapper formTemplateMapper, @Qualifier("transactionTemplate") TransactionTemplate transactionTemplate, PublishSpotMapper publishSpotMapper, @Qualifier("slaveTransactionTemplate") TransactionTemplate slaveTransactionTemplate, DynamicRegisterInfoMapper dynamicRegisterInfoMapper, MoreTransactionService moreTransactionService, EnterpriseMapper enterpriseMapper, SysJobService sysJobService, SpotInfoMapper spotInfoMapper, ActivityInfoMapper activityInfoMapper, TicketTemplateMapper ticketTemplateMapper, InterviewTicketTemplateMapper interviewTicketTemplateMapper) {
        this.registerPublishMapper = registerPublishMapper;
        this.formTemplateMapper = formTemplateMapper;
        this.transactionTemplate = transactionTemplate;
        this.publishSpotMapper = publishSpotMapper;
        this.slaveTransactionTemplate = slaveTransactionTemplate;
        this.dynamicRegisterInfoMapper = dynamicRegisterInfoMapper;
        this.moreTransactionService = moreTransactionService;
        this.enterpriseMapper = enterpriseMapper;
        this.sysJobService = sysJobService;
        this.spotInfoMapper = spotInfoMapper;
        this.activityInfoMapper = activityInfoMapper;
        this.ticketTemplateMapper = ticketTemplateMapper;
        this.interviewTicketTemplateMapper = interviewTicketTemplateMapper;
    }

    public I18nResult<Boolean> add(RegisterPublishEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String loginUsername = SecurityUtil.getLoginUsername();
            String enterpriseId = SecurityUtil.getEnterpriseId();
            Boolean payFlag = editParam.getPayFlag();
            Long cost = editParam.getCost();
            String title = editParam.getTitle();
            String description = editParam.getDescription();
            Long startAt = editParam.getStartAt();
            Long endAt = editParam.getEndAt();
            Boolean activityFlag = editParam.getActivityFlag();
            List<ActivityInfoEditParam> activityInfoEditParams = editParam.getActivityInfoEditParams();
            Boolean ticketFlag = editParam.getTicketFlag();
            TicketTemplateFlagEnum ticketTemplateFlag = editParam.getTicketTemplateFlag();
            Long ticketStartAt = editParam.getTicketStartAt();
            Long ticketEndAt = editParam.getTicketEndAt();

            Boolean scoreFlag = editParam.getScoreFlag();
            BigDecimal scoreProportion = editParam.getScoreProportion();
            Long scoreStartAt = editParam.getScoreStartAt();
            Long scoreEndAt = editParam.getScoreEndAt();

            Boolean interviewTicketFlag = editParam.getInterviewTicketFlag();
            Long interviewTicketStartAt = editParam.getInterviewTicketStartAt();
            Long interviewTicketEndAt = editParam.getInterviewTicketEndAt();
            InterviewTicketTemplateFlagEnum interviewTicketTemplateFlag = editParam.getInterviewTicketTemplateFlag();

            Boolean interviewScoreFlag = editParam.getInterviewScoreFlag();
            Long interviewScoreStartAt = editParam.getInterviewScoreStartAt();
            Long interviewScoreEndAt = editParam.getInterviewScoreEndAt();
            BigDecimal interviewScoreProportion = editParam.getInterviewScoreProportion();


            Long timeMillis = DateUtil.currentTimeMillis();
            FormTemplateFlagEnum templateFlag = editParam.getTemplateFlag();
            List<Long> timeLine = new ArrayList<>();
            timeLine.add(timeMillis);
            if (StringUtils.isAnyEmpty(enterpriseId, title, description) || startAt == null
                    || endAt == null || activityFlag == null || ticketFlag == null || scoreFlag == null || templateFlag == null
                    || interviewTicketFlag == null || interviewScoreFlag == null) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            if (Boolean.TRUE.equals(payFlag) && (cost == null || cost == 0 || cost < 0)) {
                return result.failedBadRequest().setMessage("请输入正确价格");

            }
            if (startAt < timeMillis) {
                return result.failedBadRequest().setMessage("报名开始时间不能小于当前时间");
            }
            FormTemplateModel formTemplateModel = formTemplateMapper.queryOneByFlag(templateFlag);
            if (formTemplateModel == null) {
                return result.failedBadRequest().setMessage("请选择正确的模板");
            }
            timeLine.add(startAt);
            timeLine.add(endAt);
            String uniqueId = UniqueUtil.getUniqueId();
            editParam.setId(uniqueId);
            if (Boolean.TRUE.equals(ticketFlag)) {
                if (ticketStartAt == null || ticketEndAt == null || ticketTemplateFlag == null) {
                    return result.failedBadRequest().setMessage("缺失打印证件时间信息");
                }
                TicketTemplateModel templateModel = ticketTemplateMapper.queryOneByFlag(ticketTemplateFlag);
                if (templateModel == null) {
                    return result.failedBadRequest().setMessage("请选择正确的证件模板");
                }
                timeLine.add(ticketStartAt);
                timeLine.add(ticketEndAt);
            }

            if (Boolean.TRUE.equals(activityFlag)) {
                if (CollectionUtils.isEmpty(activityInfoEditParams)) {
                    return result.failedBadRequest().setMessage("缺失活动时间信息");
                }
                for (ActivityInfoEditParam activityInfoEditParam : activityInfoEditParams) {
                    if (activityInfoEditParam == null) {
                        return result.failedBadRequest().setMessage("活动时间信息存在null实体");
                    }
                    Long activityStartAt = activityInfoEditParam.getActivityStartAt();
                    Long activityEndAt = activityInfoEditParam.getActivityEndAt();
                    String subject = activityInfoEditParam.getSubject();
                    BigDecimal proportion = activityInfoEditParam.getScoreProportion();
                    if (StringUtils.isEmpty(subject) || activityStartAt == null || activityEndAt == null || proportion == null) {
                        return result.failedBadRequest().setMessage("活动时间信息存在null字段");
                    }
                    timeLine.add(activityStartAt);
                    timeLine.add(activityEndAt);
                    activityInfoEditParam.setId(UniqueUtil.getUniqueId());
                    activityInfoEditParam.setCreateBy(loginUsername);
                    activityInfoEditParam.setCreateAt(timeMillis);
                }
            }

            if (Boolean.TRUE.equals(scoreFlag)) {
                if (scoreStartAt == null || scoreEndAt == null || scoreProportion == null) {
                    return result.failedBadRequest().setMessage("缺失查询成绩时间信息");
                }
                timeLine.add(scoreStartAt);
                timeLine.add(scoreEndAt);
            }

            if (Boolean.TRUE.equals(interviewTicketFlag)) {
                if (interviewTicketStartAt == null || interviewTicketEndAt == null || interviewTicketTemplateFlag == null) {
                    return result.failedBadRequest().setMessage("缺失面试凭证时间信息");
                }
                InterviewTicketTemplateModel interviewTicketTemplateModel = interviewTicketTemplateMapper.queryOneByFlag(interviewTicketTemplateFlag);
                if (interviewTicketTemplateModel == null) {
                    return result.failedBadRequest().setMessage("请选择正确的面试凭证模板");
                }
                timeLine.add(interviewTicketStartAt);
                timeLine.add(interviewTicketEndAt);
            }

            if (Boolean.TRUE.equals(interviewScoreFlag)) {
                if (interviewScoreProportion == null || interviewScoreStartAt == null || interviewScoreEndAt == null) {
                    return result.failedBadRequest().setMessage("缺失查询面试成绩时间信息");
                }
                timeLine.add(interviewScoreStartAt);
                timeLine.add(interviewScoreEndAt);

            }


            for (int i = 0; i < timeLine.size() - 1; i++) {
                if (timeLine.get(i) >= timeLine.get(i + 1)) {
                    return result.failedBadRequest().setMessage("时间线有问题，请核实时间的合理性");
                }
            }


            if (GaogleConfig.isRegisterPublishCostEnabled()) {
                // 计算费用
                long durationMillis = editParam.getEndAt() - editParam.getStartAt();
                BigDecimal durationMinutes = new BigDecimal(durationMillis)
                        .divide(new BigDecimal(60000), 0, RoundingMode.CEILING);
                long registerPublishCost = durationMinutes.longValue();
                long balance = enterpriseMapper.queryBalanceById(enterpriseId);
                if (balance < registerPublishCost) {
                    return result.failedBadRequest().setMessage("余额不足，请充值后使用！");
                }
                editParam.setRegisterPublishCost(registerPublishCost);
                editParam.setCost(cost * 100);
            }
            editParam.setAllocateSpotFlag(false);
            editParam.setDelFlag(false);
            editParam.setOfferStatusShowFlag(false);
            editParam.setCreateBy(loginUsername);
            editParam.setUpdateBy(loginUsername);
            editParam.setEnterpriseId(enterpriseId);
            editParam.setCreateAt(timeMillis);
            editParam.setUpdateAt(timeMillis);

            String templateContent = formTemplateModel.getContent();
            editParam.setTemplateCopy(templateContent);
            ObjectNode objectNode = JsonUtil.parseObjectNode(templateContent);
            moreTransactionService.registerPublish(objectNode, editParam);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("添加发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "添加发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> enterprisePut(RegisterPublishEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String id = editParam.getId();
            String title = editParam.getTitle();
            String description = editParam.getDescription();
            String enterpriseId = SecurityUtil.getEnterpriseId();
            String loginUsername = SecurityUtil.getLoginUsername();
            Long timeMillis = DateUtil.currentTimeMillis();
            if (StringUtils.isAnyEmpty(id, title, description, enterpriseId)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneByIdAndEnterpriseId(id, enterpriseId);
            if (registerPublishModel == null) {
                return result.failedBadRequest().setMessage("未查询到报名发布信息");
            }
            List<Long> timeLine = new ArrayList<>();
            Long endAt = registerPublishModel.getEndAt();
            timeLine.add(endAt);
            // 打印证件时间信息
            Boolean ticketFlag = registerPublishModel.getTicketFlag();
            Long ticketStartAt = editParam.getTicketStartAt();
            Long ticketEndAt = editParam.getTicketEndAt();
            if (Boolean.TRUE.equals(ticketFlag)) {
                if (ticketStartAt == null || ticketEndAt == null) {
                    return result.failedBadRequest().setMessage("缺失打印证件时间信息");
                }
                Long oldTicketStartAt = registerPublishModel.getTicketStartAt();
                Long oldTicketEndAt = registerPublishModel.getTicketEndAt();
                if (!Objects.equals(oldTicketStartAt, ticketStartAt)) {
                    if (oldTicketStartAt < timeMillis || ticketStartAt < timeMillis) {
                        return result.failedBadRequest().setMessage("打印证件开始时间已过不允许修改");
                    }
                    timeLine.add(ticketStartAt);
                } else {
                    timeLine.add(oldTicketStartAt);
                }
                if (!Objects.equals(oldTicketEndAt, ticketEndAt)) {
                    if (oldTicketEndAt < timeMillis || ticketEndAt < timeMillis) {
                        return result.failedBadRequest().setMessage("打印证件结束时间已过不允许修改");
                    }
                    timeLine.add(ticketEndAt);
                } else {
                    timeLine.add(oldTicketEndAt);
                }
            }

            // 考试时间
            Boolean activityFlag = registerPublishModel.getActivityFlag();
            List<ActivityInfoEditParam> activityInfoEditParams = editParam.getActivityInfoEditParams();
            if (Boolean.TRUE.equals(activityFlag)) {
                List<ActivityInfoModel> activityInfoModels = activityInfoMapper.queryByRegisterPublishId(id);
                if (CollectionUtils.isEmpty(activityInfoEditParams) || CollectionUtils.isEmpty(activityInfoModels)) {
                    return result.failedBadRequest().setMessage("缺失活动时间信息");
                }
                if (!Objects.equals(activityInfoModels.size(), activityInfoEditParams.size())) {
                    return result.failedBadRequest().setMessage("禁止修改活动场数");
                }
                Map<String, ActivityInfoModel> activityInfoModelMap =
                        activityInfoModels.stream()
                                .collect(Collectors.toMap(
                                        ActivityInfoModel::getId,
                                        Function.identity(),
                                        (v1, v2) -> v1 // 重复 key 时保留第一个
                                ));
                for (ActivityInfoEditParam activityInfoEditParam : activityInfoEditParams) {
                    String infoEditParamId = activityInfoEditParam.getId();
                    ActivityInfoModel activityInfoModel = activityInfoModelMap.getOrDefault(infoEditParamId, null);
                    if (activityInfoModel == null) {
                        return result.failedBadRequest().setMessage("活动信息不存在");
                    }
                    String subject = activityInfoEditParam.getSubject();
                    Long activityStartAt = activityInfoEditParam.getActivityStartAt();
                    Long activityEndAt = activityInfoEditParam.getActivityEndAt();
                    BigDecimal scoreProportion = activityInfoEditParam.getScoreProportion();

                    if (StringUtils.isEmpty(subject) || activityStartAt == null || activityEndAt == null || scoreProportion == null) {
                        return result.failedBadRequest().setMessage("缺失活动信息必要参数");
                    }

                    String oldSubject = activityInfoModel.getSubject();
                    Long oldActivityStartAt = activityInfoModel.getActivityStartAt();
                    Long oldActivityEndAt = activityInfoModel.getActivityEndAt();
                    if (!Objects.equals(oldActivityStartAt, activityStartAt)) {
                        if (oldActivityStartAt < timeMillis || activityStartAt < timeMillis) {
                            return result.failedBadRequest().setMessage("活动开始时间已过不允许修改");
                        }
                        if (Boolean.TRUE.equals(registerPublishModel.getTicketFlag()) && registerPublishModel.getTicketStartAt() < timeMillis) {
                            return result.failedBadRequest().setMessage("已过准考证开始打印时间，禁止修改活动开始时间");
                        }
                        timeLine.add(activityStartAt);
                    } else {
                        timeLine.add(oldActivityStartAt);
                    }
                    if (!Objects.equals(oldActivityEndAt, activityEndAt)) {
                        if (oldActivityEndAt < timeMillis || activityEndAt < timeMillis) {
                            return result.failedBadRequest().setMessage("活动结束时间已过不允许修改");
                        }
                        if (Boolean.TRUE.equals(registerPublishModel.getTicketFlag()) && registerPublishModel.getTicketStartAt() < timeMillis) {
                            return result.failedBadRequest().setMessage("已过准考证开始打印时间，禁止修改活动结束时间");
                        }
                        timeLine.add(activityEndAt);
                    } else {
                        timeLine.add(oldActivityEndAt);
                    }
                    if (!Objects.equals(subject, oldSubject)) {
                        if (Boolean.TRUE.equals(registerPublishModel.getTicketFlag()) && registerPublishModel.getTicketStartAt() < timeMillis) {
                            return result.failedBadRequest().setMessage("已过准考证开始打印时间，禁止修改科目");
                        }
                    }
                    activityInfoEditParam.setUpdateBy(loginUsername);
                    activityInfoEditParam.setUpdateAt(timeMillis);
                }
            }
            // 笔试成绩时间
            Boolean scoreFlag = registerPublishModel.getScoreFlag();
            Long scoreStartAt = editParam.getScoreStartAt();
            Long scoreEndAt = editParam.getScoreEndAt();
            BigDecimal scoreProportion = editParam.getScoreProportion();
            if (Boolean.TRUE.equals(scoreFlag)) {
                if (scoreStartAt == null || scoreEndAt == null || scoreProportion == null) {
                    return result.failedBadRequest().setMessage("缺失查询成绩时间信息");
                }
                timeLine.add(scoreStartAt);
                timeLine.add(scoreEndAt);
            }
            // 面试证件时间信息
            Boolean interviewTicketFlag = registerPublishModel.getInterviewTicketFlag();
            Long interviewTicketStartAt = editParam.getInterviewTicketStartAt();
            Long interviewTicketEndAt = editParam.getInterviewTicketEndAt();
            if (Boolean.TRUE.equals(interviewTicketFlag)) {
                if (interviewTicketStartAt == null || interviewTicketEndAt == null) {
                    return result.failedBadRequest().setMessage("缺失打印面试证件时间信息");
                }
                Long oldInterviewTicketStartAt = registerPublishModel.getInterviewTicketStartAt();
                Long oldInterviewTicketEndAt = registerPublishModel.getInterviewTicketEndAt();
                if (!Objects.equals(oldInterviewTicketStartAt, interviewTicketStartAt)) {
                    if (oldInterviewTicketStartAt < timeMillis || interviewTicketStartAt < timeMillis) {
                        return result.failedBadRequest().setMessage("打印面试证件开始时间已过不允许修改");
                    }
                    timeLine.add(interviewTicketStartAt);
                } else {
                    timeLine.add(oldInterviewTicketStartAt);
                }
                if (!Objects.equals(oldInterviewTicketEndAt, interviewTicketEndAt)) {
                    if (oldInterviewTicketEndAt < timeMillis || interviewTicketEndAt < timeMillis) {
                        return result.failedBadRequest().setMessage("打印面试证件结束时间已过不允许修改");
                    }
                    timeLine.add(interviewTicketEndAt);
                } else {
                    timeLine.add(oldInterviewTicketEndAt);
                }
            }
            // 面试时间信息
            Boolean interviewScoreFlag = registerPublishModel.getInterviewScoreFlag();
            Long interviewScoreStartAt = editParam.getInterviewScoreStartAt();
            Long interviewScoreEndAt = editParam.getInterviewScoreEndAt();
            BigDecimal interviewScoreProportion = editParam.getInterviewScoreProportion();
            if (Boolean.TRUE.equals(interviewScoreFlag)) {
                if (interviewScoreStartAt == null || interviewScoreEndAt == null || interviewScoreProportion == null) {
                    return result.failedBadRequest().setMessage("缺失查询面试成绩时间信息");
                }
                timeLine.add(interviewScoreStartAt);
                timeLine.add(interviewScoreEndAt);
            }

            // 时间线
            for (int i = 0; i < timeLine.size() - 1; i++) {
                if (timeLine.get(i) >= timeLine.get(i + 1)) {
                    return result.failedBadRequest().setMessage("时间线有问题，请核实时间的合理性");
                }
            }
            editParam.setUpdateBy(loginUsername);
            editParam.setUpdateAt(timeMillis);
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(@NonNull TransactionStatus status) {
                    registerPublishMapper.enterpriseUpdate(editParam);
                    if (Boolean.TRUE.equals(activityFlag)) {
                        for (ActivityInfoEditParam activityInfoEditParam : activityInfoEditParams) {
                            activityInfoMapper.enterpriseUpdate(activityInfoEditParam);
                        }
                    }
                }
            });
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("修改发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "修改发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> enterpriseAllocateSpot(RegisterPublishEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String id = editParam.getId();
            String enterpriseId = SecurityUtil.getEnterpriseId();
            Integer admissionTicketNumberLength = editParam.getAdmissionTicketNumberLength();
            String prefix = editParam.getPrefix();
            String loginUsername = SecurityUtil.getLoginUsername();
            Long timeMillis = DateUtil.currentTimeMillis();
            if (StringUtils.isAnyEmpty(id, enterpriseId) || admissionTicketNumberLength == null) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            if (admissionTicketNumberLength > 15) {
                return result.failedBadRequest().setMessage("总长度不能超过15");
            }
            if (StringUtils.isNotEmpty(prefix) && admissionTicketNumberLength <= prefix.length()) {
                return result.failedBadRequest().setMessage("总长度要大于前缀长度");
            }

            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneByIdAndEnterpriseId(id, enterpriseId);
            if (registerPublishModel == null) {
                return result.failedBadRequest().setMessage("未查询到报名发布信息");
            }
            Boolean ticketFlag = registerPublishModel.getTicketFlag();
            if (!Boolean.TRUE.equals(ticketFlag)) {
                return result.failedBadRequest().setMessage("未开启打印证件信息禁止操作");
            }

            Long endAt = registerPublishModel.getEndAt();
            if (timeMillis <= endAt) {
                return result.failedBadRequest().setMessage("报名时间未截止，禁止操作");
            }
            Boolean allocateSpotFlag = registerPublishModel.getAllocateSpotFlag();

            Long ticketStartAt = registerPublishModel.getTicketStartAt();
            // 10分钟转换为毫秒
            long tenMinutesInMillis = 10 * 60 * 1000;
            if (Boolean.TRUE.equals(allocateSpotFlag) && (timeMillis + tenMinutesInMillis) > ticketStartAt) {
                return result.failedBadRequest().setMessage("打印证件开始时间不足十分钟或已开始，禁止操作");
            }

            FormTemplateFlagEnum templateFlag = registerPublishModel.getTemplateFlag();

            FormTemplateModel formTemplateModel = formTemplateMapper.queryOneByFlag(templateFlag);
            if (formTemplateModel == null) {
                return result.failedBadRequest().setMessage("未查询到表单模板信息");
            }
            String allocateContent = formTemplateModel.getAllocateContent();
            List<String> sortOrders = new ArrayList<>();
            if (StringUtils.isNotEmpty(allocateContent)) {
                ObjectNode templateCopyNode = JsonUtil.parseObjectNode(allocateContent);
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
            String tableName = REGISTER_INFO_TABLE_NAME + id;
            List<String> createBys = dynamicRegisterInfoMapper.
                    queryCreateBysByIdAndStatusForAllocateSpot(tableName, RegisterInfoStatusEnum.VALID.value(), String.join(",", sortOrders));
            if (CollectionUtils.isEmpty(createBys)) {
                return result.failedBadRequest().setMessage("报名成功人数为0，禁止操作");
            }
            int registerNum = createBys.size();
            int realLength = admissionTicketNumberLength;
            if (StringUtils.isNotEmpty(prefix)) {
                realLength = admissionTicketNumberLength - prefix.length();
            }
            long maxNumber = (long) Math.pow(10, realLength) - 1;

            if (registerNum > maxNumber) {
                return result.failedBadRequest().setMessage("准考证编号不足，" + "总编号数" + maxNumber + ",报名人数" + registerNum);
            }
            List<PublishSpotModel> publishSpotModels = publishSpotMapper.queryByRegisterPublishIdForAllocateSpot(id);
            int totalSeatNum = 0;
            List<AllocateSpotDTO> allocateSpotDTOS = new ArrayList<>();
            long num = 1;
            for (PublishSpotModel publishSpotModel : publishSpotModels) {
                String spotId = publishSpotModel.getId();
                String spot = publishSpotModel.getSpot();
                String spotAddress = publishSpotModel.getSpotAddress();
                Integer roomQuantity = publishSpotModel.getRoomQuantity();
                Integer seatQuantity = publishSpotModel.getSeatQuantity();
                totalSeatNum = totalSeatNum + (roomQuantity * seatQuantity);
                for (Integer room = 1; room <= roomQuantity; room++) {
                    for (Integer seatNum = 1; seatNum <= seatQuantity; seatNum++) {
                        AllocateSpotDTO allocateSpotDTO = new AllocateSpotDTO();
                        allocateSpotDTO.setSpotId(spotId);
                        allocateSpotDTO.setSpot(spot);
                        allocateSpotDTO.setSpotAddress(spotAddress);
                        allocateSpotDTO.setRoomNumber(room);
                        allocateSpotDTO.setSeatNumber(seatNum);
                        String ticketNum = String.format("%0" + realLength + "d", num);
                        if (StringUtils.isNotEmpty(prefix)) {
                            ticketNum = prefix + ticketNum;
                        }
                        allocateSpotDTO.setAdmissionTicketNumber(ticketNum);
                        allocateSpotDTOS.add(allocateSpotDTO);
                        num++;
                    }
                }
            }
            if (registerNum > totalSeatNum) {
                return result.failedBadRequest().setMessage("考点总座位数不足，" + "总座位数" + totalSeatNum + ",报名人数" + registerNum);
            }
            moreTransactionService.allocateSpot(id, createBys, allocateSpotDTOS, loginUsername);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("修改发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "修改发生异常");
        }
        return result;
    }

    public I18nResult<PageModel<RegisterPublishModel>> queryByPageAndCondition(RegisterPublishQueryParam queryParam) {
        I18nResult<PageModel<RegisterPublishModel>> result = I18nResult.newInstance();
        try {
            PageMethod.startPage(queryParam.getPageNum(), queryParam.getPageSize());
            queryParam.setDelFlag(false);
            List<RegisterPublishModel> registerPublishModels = registerPublishMapper.queryByPageAndCondition(queryParam);
            result.succeed().setData(new PageModel<>(registerPublishModels));
        } catch (Exception e) {
            log.error("分页条件查询发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "分页条件查询发生异常");
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
            Long timeMillis = DateUtil.currentTimeMillis();
            registerPublishMapper.updateDelFlagByIdAndEnterpriseId(id, enterpriseId, true, timeMillis);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("根据id删除发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "根据id删除发生异常");
        }
        return result;
    }

    public I18nResult<RegisterPublishModel> queryOneById(String id) {
        I18nResult<RegisterPublishModel> result = I18nResult.newInstance();
        try {
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneById(id);
            if (registerPublishModel == null) {
                return result.failedBadRequest().setMessage("未查询到该场次");
            }
            List<ActivityInfoModel> activityInfoModels = activityInfoMapper.queryByRegisterPublishId(registerPublishModel.getId());
            registerPublishModel.setActivityInfoModels(activityInfoModels);
            Boolean payFlag = registerPublishModel.getPayFlag();
            Long cost = registerPublishModel.getCost();
            if (Boolean.TRUE.equals(payFlag) && (cost != null)) {
                registerPublishModel.setStrCost(StringUtil.amountLong2String(cost));
            }
            result.succeed().setData(registerPublishModel);
        } catch (Exception e) {
            log.error("根据id查询详情发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "根据id查询详情发生异常");
        }
        return result;
    }

    public I18nResult<PageModel<RegisterPublishModel>> enterpriseQueryByPageAndCondition(RegisterPublishQueryParam queryParam) {
        I18nResult<PageModel<RegisterPublishModel>> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isEmpty(enterpriseId)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            PageMethod.startPage(queryParam.getPageNum(), queryParam.getPageSize());
            queryParam.setEnterpriseId(enterpriseId);
            queryParam.setDelFlag(false);
            List<RegisterPublishModel> registerPublishModels = registerPublishMapper.queryByPageAndCondition(queryParam);
            for (RegisterPublishModel registerPublishModel : registerPublishModels) {
                String registerPublishId = registerPublishModel.getId();
                String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
                Integer validCount = dynamicRegisterInfoMapper.queryValidCountByStatus(tableName, RegisterInfoStatusEnum.VALID.value());
                registerPublishModel.setRegisterInfoValidCount(validCount);
            }
            result.succeed().setData(new PageModel<>(registerPublishModels));
        } catch (Exception e) {
            log.error("分页条件查询发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "分页条件查询发生异常");
        }
        return result;
    }

    public I18nResult<PageModel<RegisterPublishModel>> clientQueryByPageAndCondition(RegisterPublishQueryParam queryParam) {
        I18nResult<PageModel<RegisterPublishModel>> result = I18nResult.newInstance();
        try {
            String enterpriseId = queryParam.getEnterpriseId();
            if (StringUtils.isEmpty(enterpriseId)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("缺失必要参数");
            }
            PageMethod.startPage(queryParam.getPageNum(), queryParam.getPageSize());
            queryParam.setEnterpriseId(enterpriseId);
            queryParam.setDelFlag(false);
            List<RegisterPublishModel> registerPublishModels = registerPublishMapper.queryByPageAndCondition(queryParam);
            result.succeed().setData(new PageModel<>(registerPublishModels));
        } catch (Exception e) {
            log.error("分页条件查询发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "分页条件查询发生异常");
        }
        return result;
    }

    public I18nResult<PageModel<UserRegisterDTO>> clientQueryRegisterByPageAndCondition(RegisterPublishQueryParam queryParam) {
        I18nResult<PageModel<UserRegisterDTO>> result = I18nResult.newInstance();
        try {
            String accountBy = SecurityUtil.getLoginUsername();
            if (StringUtils.isAnyEmpty(accountBy)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            queryParam.setAccountBy(accountBy);
            queryParam.setDelFlag(false);
            PageMethod.startPage(queryParam.getPageNum(), queryParam.getPageSize());
            List<UserRegisterDTO> registerPublishModels = registerPublishMapper.queryRegisterByPageAndCondition(queryParam);
            result.succeed().setData(new PageModel<>(registerPublishModels));
        } catch (Exception e) {
            log.error("分页条件查询发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "分页条件查询发生异常");
        }
        return result;
    }

    public I18nResult<List<ActivityInfoModel>> enterpriseQueryActivityInfo(String registerPublishId) {
        I18nResult<List<ActivityInfoModel>> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(enterpriseId, registerPublishId)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneByIdAndEnterpriseId(registerPublishId, enterpriseId);
            if (registerPublishModel == null) {
                return result.failedBadRequest().setMessage("未查询到该场次");
            }
            List<ActivityInfoModel> activityInfoModels = activityInfoMapper.queryByRegisterPublishId(registerPublishId);
            result.succeed().setData(activityInfoModels);
        } catch (Exception e) {
            log.error("查询发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "查询发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> offerPut(OfferPutDTO offerPutDTO) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String registerPublishId = offerPutDTO.getRegisterPublishId();
            Boolean offerStatusShowFlag = offerPutDTO.getOfferStatusShowFlag();
            String offerStatusShowExplain = offerPutDTO.getOfferStatusShowExplain();
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(enterpriseId, registerPublishId) || offerStatusShowFlag == null) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneByIdAndEnterpriseId(registerPublishId, enterpriseId);
            if (registerPublishModel == null) {
                return result.failedBadRequest().setMessage("未查询到该场次");
            }
            registerPublishMapper.updateOfferStatusShowFlagAndExplain(registerPublishId, offerStatusShowFlag, offerStatusShowExplain);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("录用情况展示修改发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "录用情况展示修改发生异常");
        }
        return result;
    }

    public I18nResult<PageModel<RegisterPublishModel>> clientQueryOfferShowByPageAndCondition(RegisterPublishQueryParam queryParam) {
        I18nResult<PageModel<RegisterPublishModel>> result = I18nResult.newInstance();
        try {
            String enterpriseId = queryParam.getEnterpriseId();
            if (StringUtils.isEmpty(enterpriseId)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("缺失必要参数");
            }
            PageMethod.startPage(queryParam.getPageNum(), queryParam.getPageSize());
            queryParam.setEnterpriseId(enterpriseId);
            queryParam.setOfferStatusShowFlag(true);
            queryParam.setDelFlag(false);
            List<RegisterPublishModel> registerPublishModels = registerPublishMapper.queryByPageAndCondition(queryParam);
            result.succeed().setData(new PageModel<>(registerPublishModels));
        } catch (Exception e) {
            log.error("分页条件查询发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "分页条件查询发生异常");
        }
        return result;

    }
}
