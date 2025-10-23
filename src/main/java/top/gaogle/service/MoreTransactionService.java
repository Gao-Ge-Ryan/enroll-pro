package top.gaogle.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import top.gaogle.common.KeyValue;
import top.gaogle.dao.master.*;
import top.gaogle.dao.slave.DynamicRegisterInfoMapper;
import top.gaogle.framework.annotation.MoreTransaction;
import top.gaogle.framework.config.GaogleConfig;
import top.gaogle.framework.util.DateUtil;
import top.gaogle.framework.util.JsonUtil;
import top.gaogle.framework.util.UniqueUtil;
import top.gaogle.pojo.dto.AllocateSpotDTO;
import top.gaogle.pojo.enums.*;
import top.gaogle.pojo.model.RegisterPublishModel;
import top.gaogle.pojo.param.ActivityInfoEditParam;
import top.gaogle.pojo.param.EnterpriseBillEditParam;
import top.gaogle.pojo.param.RegisterPublishEditParam;
import top.gaogle.pojo.param.RegisterUserEditParam;

import java.util.*;

import static top.gaogle.common.RegisterConst.REGISTER_INFO_TABLE_NAME;

@Service
public class MoreTransactionService extends SuperService {

    private final DynamicRegisterInfoMapper dynamicRegisterInfoMapper;
    private final RegisterPublishMapper registerPublishMapper;
    private final TransactionTemplate transactionTemplate;
    private final RegisterBillMapper registerBillMapper;
    private final EnterpriseBillMapper enterpriseBillMapper;
    private final SysJobService sysJobService;
    private final RegisterUserMapper registerUserMapper;
    private final ActivityInfoMapper activityInfoMapper;
    private final RegisterPublishAnnouncementMapper registerPublishAnnouncementMapper;
    private final PublishSpotMapper publishSpotMapper;

    @Autowired
    public MoreTransactionService(DynamicRegisterInfoMapper dynamicRegisterInfoMapper, RegisterPublishMapper registerPublishMapper, TransactionTemplate transactionTemplate, RegisterBillMapper registerBillMapper, EnterpriseBillMapper enterpriseBillMapper, SysJobService sysJobService, RegisterUserMapper registerUserMapper, ActivityInfoMapper activityInfoMapper, RegisterPublishAnnouncementMapper registerPublishAnnouncementMapper, PublishSpotMapper publishSpotMapper) {
        this.dynamicRegisterInfoMapper = dynamicRegisterInfoMapper;
        this.registerPublishMapper = registerPublishMapper;
        this.transactionTemplate = transactionTemplate;
        this.registerBillMapper = registerBillMapper;
        this.enterpriseBillMapper = enterpriseBillMapper;
        this.sysJobService = sysJobService;
        this.registerUserMapper = registerUserMapper;
        this.activityInfoMapper = activityInfoMapper;
        this.registerPublishAnnouncementMapper = registerPublishAnnouncementMapper;
        this.publishSpotMapper = publishSpotMapper;
    }

    @MoreTransaction(value = {"transactionManager", "slaveTransactionManager"})
    public boolean registerPublish(ObjectNode objectNode, RegisterPublishEditParam editParam)  {
        List<ActivityInfoEditParam> activityInfoEditParams = editParam.getActivityInfoEditParams();
        Boolean activityFlag = editParam.getActivityFlag();
        String uniqueId = editParam.getId();
        String tableName = REGISTER_INFO_TABLE_NAME + uniqueId;
        Iterator<Map.Entry<String, JsonNode>> iterator = objectNode.fields();
        List<Map<String, Object>> columns = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> next = iterator.next();
            JsonNode jsonNode = next.getValue();
            Map<String, Object> column = new HashMap<>();
            column.put("name", jsonNode.get("key").asText());
            column.put("type", jsonNode.get("type").asText());
            column.put("comment", jsonNode.get("remark").asText());
            columns.add(column);
        }
        editParam.setStatus(RegisterPublishStatusEnum.NEW);
        editParam.setPublishStatus(RegisterPublishPublishStatusEnum.PUBLISHED);
        registerPublishMapper.insert(editParam);
        if (Boolean.TRUE.equals(activityFlag) && CollectionUtils.isNotEmpty(activityInfoEditParams)) {
            for (ActivityInfoEditParam activityInfoEditParam : activityInfoEditParams) {
                activityInfoEditParam.setRegisterPublishId(editParam.getId());
                activityInfoMapper.insert(activityInfoEditParam);
            }
        }
        if (GaogleConfig.isRegisterPublishCostEnabled()) {
            Long registerPublishCost = editParam.getRegisterPublishCost();
            EnterpriseBillEditParam billEditParam = new EnterpriseBillEditParam();
            String billUniqueId = UniqueUtil.getUniqueId();
            billEditParam.setId(billUniqueId);
            billEditParam.setEnterpriseId(editParam.getEnterpriseId());
            billEditParam.setType(EnterpriseBillTypeEnum.EXPENDITURE);
            billEditParam.setStatus(BillStatusEnum.INIT);
            billEditParam.setAmount(registerPublishCost);
            billEditParam.setSubject("报名发布");
            billEditParam.setSystemComment(JsonUtil.object2Json(KeyValue.create().entry("RegisterPublishId", uniqueId)
                    .entry("RegisterPublishTitle", editParam.getTitle())));
            billEditParam.setCreateBy(editParam.getCreateBy());
            billEditParam.setCreateAt(editParam.getCreateAt());
            enterpriseBillMapper.insert(billEditParam);
            enterpriseBillMapper.updateBalanceAndStatusSubtractByBillId(billUniqueId, registerPublishCost, BillStatusEnum.VALID, DateUtil.currentTimeMillis());
        }
        dynamicRegisterInfoMapper.createTableDynamic(tableName, columns);
        return true;
    }

    @MoreTransaction(value = {"transactionManager", "slaveTransactionManager"})
    public boolean updateClientRegisterBill(String billId, BillStatusEnum status, String tradeStatus, String tradeNo,
                                            Long completionAt, String registerPublishId, String createBy) {
        int billEffectRow = registerBillMapper.updateStatusByBillId(billId, BillStatusEnum.VALID,
                tradeStatus, tradeNo, DateUtil.currentTimeMillis());
        String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
        int statusEffectRow = dynamicRegisterInfoMapper.updateStatusByCreateBy(tableName, createBy, RegisterInfoStatusEnum.VALID.value());
        log.info("账单{}-用户缴费更新完成影响行数billEffectRow:{},statusEffectRow:{}", billId, billEffectRow, statusEffectRow);
        return true;
    }

    @MoreTransaction(value = {"transactionManager", "slaveTransactionManager"})
    public boolean allocateSpot(String registerPublishId, List<String> createBys, List<AllocateSpotDTO> allocateSpotDTOS, String loginUsername) {
        for (int i = 0; i < createBys.size(); i++) {
            String createBy = createBys.get(i);
            AllocateSpotDTO allocateSpotDTO = allocateSpotDTOS.get(i);
            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            dynamicRegisterInfoMapper.updateSpotByCreateBy(tableName, createBy, allocateSpotDTO);
        }
        registerPublishMapper.updateAllocateSpotFlagById(registerPublishId, true, loginUsername, DateUtil.currentTimeMillis());
        return true;
    }

    @MoreTransaction(value = {"transactionManager", "slaveTransactionManager"})
    public boolean clientApplyInfo(String tableName, List<String> columns, List<Object> values, RegisterUserEditParam editParam) {
        registerUserMapper.insert(editParam);
        dynamicRegisterInfoMapper.insertDynamic(tableName, columns, values);
        return true;
    }

    @MoreTransaction(value = {"transactionManager", "slaveTransactionManager"})
    public boolean cleanDeleteRegisterPublish(RegisterPublishModel registerPublishModel) {
        String registerPublishId = registerPublishModel.getId();
        String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
        registerPublishMapper.deleteById(registerPublishId);
        activityInfoMapper.deleteByRegisterPublishId(registerPublishId);
        registerPublishAnnouncementMapper.deleteByRegisterPublishId(registerPublishId);
        publishSpotMapper.deleteByRegisterPublishId(registerPublishId);
        registerUserMapper.deleteByRegisterPublishId(registerPublishId);
        registerBillMapper.deleteByRegisterPublishId(registerPublishId);
        dynamicRegisterInfoMapper.dropTableDynamic(tableName);
        return true;
    }
}
