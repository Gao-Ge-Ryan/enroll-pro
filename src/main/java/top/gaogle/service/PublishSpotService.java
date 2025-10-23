package top.gaogle.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.gaogle.dao.master.PublishSpotMapper;
import top.gaogle.dao.master.RegisterPublishMapper;
import top.gaogle.dao.slave.DynamicRegisterInfoMapper;
import top.gaogle.framework.i18n.I18ResultCode;
import top.gaogle.framework.i18n.I18nResult;
import top.gaogle.framework.util.*;
import top.gaogle.pojo.dto.SeatInfoDTO;
import top.gaogle.pojo.dto.StatisticsRoomSeatCountDTO;
import top.gaogle.pojo.enums.HttpStatusEnum;
import top.gaogle.pojo.model.PublishSpotModel;
import top.gaogle.pojo.model.RegisterPublishModel;
import top.gaogle.pojo.param.PublishSpotEditParam;

import java.util.*;
import java.util.stream.Collectors;

import static top.gaogle.common.RegisterConst.*;

@Component
public class PublishSpotService extends SuperService {

    private final PublishSpotMapper publishSpotMapper;
    private final RegisterPublishMapper registerPublishMapper;
    private final DynamicRegisterInfoMapper dynamicRegisterInfoMapper;

    @Autowired
    public PublishSpotService(PublishSpotMapper publishSpotMapper, RegisterPublishMapper registerPublishMapper, DynamicRegisterInfoMapper dynamicRegisterInfoMapper) {
        this.publishSpotMapper = publishSpotMapper;
        this.registerPublishMapper = registerPublishMapper;
        this.dynamicRegisterInfoMapper = dynamicRegisterInfoMapper;
    }

    public I18nResult<Boolean> insert(PublishSpotEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String spot = editParam.getSpot();
            String spotAddress = editParam.getSpotAddress();
            Integer roomQuantity = editParam.getRoomQuantity();
            Integer seatQuantity = editParam.getSeatQuantity();
            String registerPublishId = editParam.getRegisterPublishId();
            if (StringUtils.isAnyEmpty(spot, spotAddress, registerPublishId) || roomQuantity == null || seatQuantity == null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage(I18ResultCode.MESSAGE, "缺少必要参数");
            }
            String enterpriseId = SecurityUtil.getEnterpriseId();
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneById(registerPublishId);
            if (registerPublishModel == null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("发布信息不存在");
            }
            String publishEnterpriseId = registerPublishModel.getEnterpriseId();
            if (!Objects.equals(publishEnterpriseId, enterpriseId)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("没有权限操作");
            }
            Boolean allocateSpotFlag = registerPublishModel.getAllocateSpotFlag();
            if (Boolean.TRUE.equals(allocateSpotFlag)) {
                return result.failedBadRequest().setMessage("报名考点已分配禁止操作");
            }
            PublishSpotModel model = publishSpotMapper.queryBySpotAndRegisterPublishId(spot, registerPublishId);
            if (model != null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage(spot + " 考点名称已存在");
            }
            editParam.setEnterpriseId(enterpriseId);
            editParam.setId(UniqueUtil.getUniqueId());
            Long timeMillis = DateUtil.currentTimeMillis();
            editParam.setCreateAt(timeMillis);
            editParam.setUpdateAt(timeMillis);
            String loginUsername = SecurityUtil.getLoginUsername();
            editParam.setCreateBy(loginUsername);
            editParam.setUpdateBy(loginUsername);
            publishSpotMapper.insert(editParam);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("添加发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "添加发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> putSpotInfo(PublishSpotEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String id = editParam.getId();
            String spot = editParam.getSpot();
            String spotAddress = editParam.getSpotAddress();
            Integer roomQuantity = editParam.getRoomQuantity();
            Integer seatQuantity = editParam.getSeatQuantity();
            String registerPublishId = editParam.getRegisterPublishId();
            if (StringUtils.isAnyEmpty(id, spot, spotAddress, registerPublishId) || roomQuantity == null || seatQuantity == null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage(I18ResultCode.MESSAGE, "缺少必要参数");
            }
            String enterpriseId = SecurityUtil.getEnterpriseId();
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneById(registerPublishId);
            if (registerPublishModel == null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("发布信息不存在");
            }
            String publishEnterpriseId = registerPublishModel.getEnterpriseId();
            if (!Objects.equals(publishEnterpriseId, enterpriseId)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("没有权限操作");
            }
            Boolean allocateSpotFlag = registerPublishModel.getAllocateSpotFlag();
            if (Boolean.TRUE.equals(allocateSpotFlag)) {
                return result.failedBadRequest().setMessage("报名考点已分配禁止操作");
            }
            PublishSpotModel model = publishSpotMapper.queryExcludeIdBySpotAndRegisterPublishId(id, spot, registerPublishId);
            if (model != null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage(spot + " 考点名称已存在");
            }
            Long timeMillis = DateUtil.currentTimeMillis();
            String loginUsername = SecurityUtil.getLoginUsername();
            editParam.setUpdateBy(loginUsername);
            editParam.setUpdateAt(timeMillis);
            editParam.setEnterpriseId(SecurityUtil.getEnterpriseId());
            publishSpotMapper.putSpotInfo(editParam);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("修改发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "修改发生异常");
        }
        return result;
    }

    public I18nResult<PublishSpotModel> queryOneById(String id) {
        I18nResult<PublishSpotModel> result = I18nResult.newInstance();
        try {
            PublishSpotModel enterpriseModel = publishSpotMapper.queryOneById(id);
            result.succeed().setData(enterpriseModel);
        } catch (Exception e) {
            log.error("根据id查询发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "根据id查询发生异常");
        }
        return result;
    }

    public I18nResult<List<PublishSpotModel>> queryByRegisterPublishId(String registerPublishId) {
        I18nResult<List<PublishSpotModel>> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(enterpriseId, registerPublishId)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("缺少必要参数");
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneById(registerPublishId);
            if (registerPublishModel == null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("发布信息不存在");
            }
            String publishEnterpriseId = registerPublishModel.getEnterpriseId();
            if (!Objects.equals(publishEnterpriseId, enterpriseId)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("没有权限操作");
            }
            Boolean allocateSpotFlag = registerPublishModel.getAllocateSpotFlag();
            List<PublishSpotModel> spotInfoModels = publishSpotMapper.queryByRegisterPublishId(registerPublishId);
            if (CollectionUtils.isNotEmpty(spotInfoModels) && Boolean.TRUE.equals(allocateSpotFlag)) {
                String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
                List<StatisticsRoomSeatCountDTO> statisticsRoomSeatCount = dynamicRegisterInfoMapper.statisticsRoomSeatCount(tableName);
                if (CollectionUtils.isNotEmpty(statisticsRoomSeatCount)) {
                    Map<String, StatisticsRoomSeatCountDTO> spotIdToDtoMap = statisticsRoomSeatCount.stream()
                            .collect(Collectors.toMap(
                                    StatisticsRoomSeatCountDTO::getSpotId,
                                    dto -> dto,
                                    (dto1, dto2) -> dto1  // 如果有重复 spotId，保留第一个
                            ));

                    for (PublishSpotModel spotInfoModel : spotInfoModels) {
                        StatisticsRoomSeatCountDTO spotIdToDto = spotIdToDtoMap.getOrDefault(spotInfoModel.getId(), null);
                        if (spotIdToDto != null) {
                            spotInfoModel.setRoomCount(spotIdToDto.getRoomCount());
                            spotInfoModel.setSeatCount(spotIdToDto.getSeatCount());
                        }
                    }
                }
            }
            result.succeed().setData(spotInfoModels);
        } catch (Exception e) {
            log.error("查询发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "查询发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> deleteById(String id, String registerPublishId) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneById(registerPublishId);
            if (registerPublishModel == null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("发布信息不存在");
            }
            String publishEnterpriseId = registerPublishModel.getEnterpriseId();
            if (!Objects.equals(publishEnterpriseId, enterpriseId)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("没有权限操作");
            }
            Boolean allocateSpotFlag = registerPublishModel.getAllocateSpotFlag();
            if (Boolean.TRUE.equals(allocateSpotFlag)) {
                return result.failedBadRequest().setMessage("报名考点已分配禁止操作");
            }
            publishSpotMapper.deleteByIdAndEnterpriseId(id, enterpriseId);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("删除发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "删除发生异常");
        }
        return result;
    }

    public I18nResult<List<String>> queryRoomByRegisterPublishId(String registerPublishId, String spotId) {
        I18nResult<List<String>> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(enterpriseId, registerPublishId, spotId)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("缺少必要参数");
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneById(registerPublishId);
            if (registerPublishModel == null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("发布信息不存在");
            }
            String publishEnterpriseId = registerPublishModel.getEnterpriseId();
            if (!Objects.equals(publishEnterpriseId, enterpriseId)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("没有权限操作");
            }
            Boolean allocateSpotFlag = registerPublishModel.getAllocateSpotFlag();
            if (!Boolean.TRUE.equals(allocateSpotFlag)) {
                return result.failedBadRequest().setMessage("报名考点未分配禁止操作");
            }
            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            List<String> roomNumbers = dynamicRegisterInfoMapper.queryRoomByRegisterPublishIdAndSpotId(tableName, spotId);
            result.succeed().setData(roomNumbers);
        } catch (Exception e) {
            log.error("删除发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "删除发生异常");
        }
        return result;


    }

    public I18nResult<List<SeatInfoDTO>> querySeatByRegisterPublishId(String registerPublishId, String spotId, String roomNumber) {
        I18nResult<List<SeatInfoDTO>> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(enterpriseId, registerPublishId, spotId, roomNumber)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("缺少必要参数");
            }
            RegisterPublishModel registerPublishModel = registerPublishMapper.queryOneById(registerPublishId);
            if (registerPublishModel == null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("发布信息不存在");
            }
            String publishEnterpriseId = registerPublishModel.getEnterpriseId();
            if (!Objects.equals(publishEnterpriseId, enterpriseId)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("没有权限操作");
            }
            Boolean allocateSpotFlag = registerPublishModel.getAllocateSpotFlag();
            if (!Boolean.TRUE.equals(allocateSpotFlag)) {
                return result.failedBadRequest().setMessage("报名考点未分配禁止操作");
            }
            String tableName = REGISTER_INFO_TABLE_NAME + registerPublishId;
            List<SeatInfoDTO> seatInfoDTOS = dynamicRegisterInfoMapper.querySeatInfoByRegisterPublishIdAndSpotId(tableName, spotId, roomNumber);
            if (CollectionUtils.isNotEmpty(seatInfoDTOS)) {
                String templateCopy = registerPublishModel.getTemplateCopy();
                List<String> fields = new ArrayList<>();
                List<Map<String, Object>> conditions = new ArrayList<>();
                Map<String, String> keyValueName = new LinkedHashMap<>();
                if (StringUtils.isNotEmpty(templateCopy)) {
                    ObjectNode templateCopyNode = JsonUtil.parseObjectNode(templateCopy);
                    Iterator<Map.Entry<String, JsonNode>> iterator = templateCopyNode.fields();
                    while (iterator.hasNext()) {
                        Map.Entry<String, JsonNode> next = iterator.next();
                        String key = next.getKey();
                        JsonNode jsonNode = next.getValue();
                        Boolean keyValue = jsonNode.get(TEMPLATE_KEY_VALUE).asBoolean(false);
                        String column = jsonNode.get(TEMPLATE_KEY).asText();
                        String remark = jsonNode.get(TEMPLATE_REMARK).asText();
                        if (Boolean.TRUE.equals(keyValue)) {
                            fields.add(column + AS + key);
                            keyValueName.put(key, remark);
                        }
                    }
                }

                Map<String, Object> spotIdCondition = new HashMap<>();
                spotIdCondition.put(KEY, COLUMN_SPOT_ID);
                spotIdCondition.put(VALUE, spotId);
                spotIdCondition.put(MATCH_TYPE, MATCH_TYPE_EXACT); // 使用模糊匹配
                conditions.add(spotIdCondition);

                Map<String, Object> condition = new HashMap<>();
                condition.put(KEY, COLUMN_ROOM_NUMBER);
                condition.put(VALUE, roomNumber);
                condition.put(MATCH_TYPE, MATCH_TYPE_EXACT); // 使用模糊匹配
                conditions.add(condition);
                if (CollectionUtils.isNotEmpty(fields)) {
                    String camelCaseCreateBy = StringUtil.snakeToCamelCase(COLUMN_CREATE_BY);
                    fields.add(COLUMN_CREATE_BY + AS + camelCaseCreateBy);
                    List<Map<String, Object>> objectNodes = dynamicRegisterInfoMapper.selectDynamic(tableName, fields, conditions);
                    if (CollectionUtils.isNotEmpty(objectNodes)) {
                        Map<String, Map<String, Object>> resultMap = objectNodes.stream()
                                .filter(map -> map.get(camelCaseCreateBy) != null)
                                .collect(Collectors.toMap(
                                        map -> map.get(camelCaseCreateBy).toString(), // key：createBy 字段
                                        map -> map,                            // value：整条记录
                                        (m1, m2) -> m1                         // 如果 key 重复，保留第一条
                                ));

                        for (SeatInfoDTO seatInfoDTO : seatInfoDTOS) {
                            String createBy = seatInfoDTO.getCreateBy();
                            List<String> keyValueList = new ArrayList<>();
                            Map<String, Object> objectMap = resultMap.getOrDefault(createBy, null);
                            if (objectMap != null) {
                                for (Map.Entry<String, String> entry : keyValueName.entrySet()) {
                                    String key = entry.getKey();
                                    String value = entry.getValue();
                                    Object obj = objectMap.getOrDefault(key, "");
                                    keyValueList.add(value + "：" + (obj == null ? "" : obj));
                                }
                                if (CollectionUtils.isNotEmpty(keyValueList)){
                                    String keyValue = String.join(";", keyValueList);
                                    seatInfoDTO.setKeyValue(keyValue);
                                }
                            }
                        }
                    }
                }
            }

            result.succeed().setData(seatInfoDTOS);
        } catch (Exception e) {
            log.error("删除发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "删除发生异常");
        }
        return result;
    }
}
