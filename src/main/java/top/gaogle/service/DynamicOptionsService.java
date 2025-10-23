package top.gaogle.service;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import top.gaogle.dao.master.DynamicOptionsMapper;
import top.gaogle.framework.i18n.I18ResultCode;
import top.gaogle.framework.i18n.I18nResult;
import top.gaogle.framework.util.DateUtil;
import top.gaogle.framework.util.SecurityUtil;
import top.gaogle.framework.util.UniqueUtil;
import top.gaogle.pojo.enums.DynamicOptionsStatusEnum;
import top.gaogle.pojo.enums.DynamicOptionsTypeEnum;
import top.gaogle.pojo.model.DynamicOptionsModel;
import top.gaogle.pojo.param.DynamicOptionsEditParam;
import top.gaogle.pojo.param.DynamicOptionsQueryParam;

import java.util.List;
import java.util.Objects;

@Service
public class DynamicOptionsService extends SuperService {
    private final DynamicOptionsMapper dynamicOptionsMapper;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public DynamicOptionsService(DynamicOptionsMapper dynamicOptionsMapper, TransactionTemplate transactionTemplate) {
        this.dynamicOptionsMapper = dynamicOptionsMapper;
        this.transactionTemplate = transactionTemplate;
    }

    public I18nResult<Boolean> insert(DynamicOptionsEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            String loginUsername = SecurityUtil.getLoginUsername();
            Long timeMillis = DateUtil.currentTimeMillis();
            String name = editParam.getName();
            String pid = editParam.getPid();
            DynamicOptionsTypeEnum type = editParam.getType();
            if (StringUtils.isAnyEmpty(name, pid, enterpriseId) || type == null) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            editParam.setId(UniqueUtil.getUniqueId());
            editParam.setStatus(DynamicOptionsStatusEnum.ENABLE);
            editParam.setEnterpriseId(enterpriseId);
            editParam.setCreateBy(loginUsername);
            editParam.setCreateAt(timeMillis);
            dynamicOptionsMapper.insert(editParam);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> put(DynamicOptionsEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            String id = editParam.getId();
            String name = editParam.getName();
            DynamicOptionsTypeEnum type = editParam.getType();
            String pid = editParam.getPid();
            if (StringUtils.isAnyEmpty(id, name, pid, enterpriseId) || type == null) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            DynamicOptionsModel dynamicOptionsModel = dynamicOptionsMapper.queryOneById(id);
            if (dynamicOptionsModel == null) {
                return result.failedBadRequest().setMessage("动态选项信息不存在");
            }
            if (!Objects.equals(enterpriseId, dynamicOptionsModel.getEnterpriseId())) {
                return result.failedBadRequest().setMessage("无权限操作");
            }
            editParam.setUpdateBy(SecurityUtil.getLoginUsername());
            editParam.setUpdateAt(DateUtil.currentTimeMillis());
            editParam.setStatus(DynamicOptionsStatusEnum.ENABLE);
            dynamicOptionsMapper.put(editParam);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> deleteById(String id) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(id, enterpriseId)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            DynamicOptionsModel dynamicOptionsModel = dynamicOptionsMapper.queryOneById(id);
            if (dynamicOptionsModel == null) {
                return result.failedBadRequest().setMessage("动态选项信息不存在");
            }
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(@NonNull TransactionStatus status) {
                    recursionDelete(id);
                }
            });
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "发生异常");
        }
        return result;
    }

    public void recursionDelete(String id) {
        List<String> childIds = dynamicOptionsMapper.queryIdsByPid(id);
        if (CollectionUtils.isNotEmpty(childIds)) {
            for (String childId : childIds) {
                recursionDelete(childId);
            }
        }
        dynamicOptionsMapper.deleteById(id);
    }

    public I18nResult<List<DynamicOptionsModel>> queryAll(DynamicOptionsQueryParam queryParam) {
        I18nResult<List<DynamicOptionsModel>> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            DynamicOptionsTypeEnum type = queryParam.getType();
            String pid = queryParam.getPid();
            if (StringUtils.isAnyEmpty(enterpriseId, pid) || type == null) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            List<DynamicOptionsModel> dynamicOptionsModels = dynamicOptionsMapper.queryAllByPidAndEnterpriseId(pid, enterpriseId, type);
            result.succeed().setData(dynamicOptionsModels);
        } catch (Exception e) {
            log.error("发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "发生异常");
        }
        return result;
    }

    public I18nResult<DynamicOptionsModel> queryOneById(String id) {
        I18nResult<DynamicOptionsModel> result = I18nResult.newInstance();
        try {
            if (StringUtils.isEmpty(id)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            DynamicOptionsModel dynamicOptionsModel = dynamicOptionsMapper.queryOneById(id);
            result.succeed().setData(dynamicOptionsModel);
        } catch (Exception e) {
            log.error("发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "发生异常");
        }
        return result;
    }

    public I18nResult<List<DynamicOptionsModel>> clientQueryAll(DynamicOptionsQueryParam queryParam) {
        I18nResult<List<DynamicOptionsModel>> result = I18nResult.newInstance();
        try {
            String enterpriseId = queryParam.getEnterpriseId();
            DynamicOptionsTypeEnum type = queryParam.getType();
            String pid = queryParam.getPid();
            if (StringUtils.isAnyEmpty(pid) || type == null) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            List<DynamicOptionsModel> dynamicOptionsModels = dynamicOptionsMapper.clientQueryAllByPidAndEnterpriseId(pid, enterpriseId, type);
            result.succeed().setData(dynamicOptionsModels);
        } catch (Exception e) {
            log.error("发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "发生异常");
        }
        return result;
    }

    public I18nResult<List<DynamicOptionsModel>> queryAllByType(DynamicOptionsQueryParam queryParam) {
        I18nResult<List<DynamicOptionsModel>> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            DynamicOptionsTypeEnum type = queryParam.getType();
            if (StringUtils.isAnyEmpty(enterpriseId) || type == null) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            List<DynamicOptionsModel> dynamicOptionsModels = dynamicOptionsMapper.queryByCondition(queryParam);
            result.succeed().setData(dynamicOptionsModels);
        } catch (Exception e) {
            log.error("发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "发生异常");
        }
        return result;
    }

    public I18nResult<List<DynamicOptionsModel>> clientQueryAllByType(DynamicOptionsQueryParam queryParam) {
        I18nResult<List<DynamicOptionsModel>> result = I18nResult.newInstance();
        try {
            DynamicOptionsTypeEnum type = queryParam.getType();
            if (type == null) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            List<DynamicOptionsModel> dynamicOptionsModels = dynamicOptionsMapper.queryByCondition(queryParam);
            result.succeed().setData(dynamicOptionsModels);
        } catch (Exception e) {
            log.error("发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "发生异常");
        }
        return result;

    }
}
