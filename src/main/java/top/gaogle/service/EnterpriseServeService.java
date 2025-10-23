package top.gaogle.service;

import com.github.pagehelper.page.PageMethod;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.gaogle.dao.master.EnterpriseServeMapper;
import top.gaogle.framework.i18n.I18ResultCode;
import top.gaogle.framework.i18n.I18nResult;
import top.gaogle.framework.pojo.PageModel;
import top.gaogle.framework.util.DateUtil;
import top.gaogle.framework.util.SecurityUtil;
import top.gaogle.framework.util.UniqueUtil;
import top.gaogle.pojo.enums.HttpStatusEnum;
import top.gaogle.pojo.model.EnterpriseServeModel;
import top.gaogle.pojo.param.EnterpriseServeEditParam;
import top.gaogle.pojo.param.EnterpriseServeQueryParam;

import java.util.List;
import java.util.Objects;

@Service
public class EnterpriseServeService extends SuperService {

    private final EnterpriseServeMapper enterpriseServeMapper;

    @Autowired
    public EnterpriseServeService(EnterpriseServeMapper enterpriseServeMapper) {
        this.enterpriseServeMapper = enterpriseServeMapper;
    }


    public I18nResult<Boolean> insert(EnterpriseServeEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            String loginUsername = SecurityUtil.getLoginUsername();
            String logo = editParam.getLogo();
            String name = editParam.getName();
            String description = editParam.getDescription();
            if (StringUtils.isAnyEmpty(logo, name, description, enterpriseId)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage(I18ResultCode.MESSAGE, "缺少必要参数");
            }
            editParam.setEnterpriseId(enterpriseId);
            editParam.setId(UniqueUtil.getUniqueId());
            Long timeMillis = DateUtil.currentTimeMillis();
            editParam.setCreateAt(timeMillis);
            editParam.setUpdateAt(timeMillis);
            editParam.setCreateBy(loginUsername);
            editParam.setUpdateBy(loginUsername);
            enterpriseServeMapper.insert(editParam);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("添加发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "添加发生异常");
        }
        return result;
    }

    public I18nResult<List<EnterpriseServeModel>> clientQueryAll(String enterpriseId) {

        I18nResult<List<EnterpriseServeModel>> result = I18nResult.newInstance();
        try {
            List<EnterpriseServeModel> models = enterpriseServeMapper.queryByEnterpriseId(enterpriseId);
            result.succeed().setData(models);
        } catch (Exception e) {
            log.error("分页条件查询发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "查询发生异常");
        }
        return result;
    }

    public I18nResult<PageModel<EnterpriseServeModel>> queryByPageAndCondition(EnterpriseServeQueryParam queryParam) {
        I18nResult<PageModel<EnterpriseServeModel>> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(enterpriseId)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            queryParam.setEnterpriseId(enterpriseId);
            PageMethod.startPage(queryParam.getPageNum(), queryParam.getPageSize());
            List<EnterpriseServeModel> commentModels = enterpriseServeMapper.queryByPageAndCondition(queryParam);
            PageModel<EnterpriseServeModel> pageModel = new PageModel<>(commentModels);
            result.succeed().setData(pageModel);
        } catch (Exception e) {
            log.error("分页条件查询发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "分页条件查询发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> put(EnterpriseServeEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String id = editParam.getId();
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(id, enterpriseId)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            EnterpriseServeModel enterprisePartner = enterpriseServeMapper.queryById(id);
            if (enterprisePartner == null) {
                return result.failedBadRequest().setMessage("实体不存在");
            }
            if (!Objects.equals(enterpriseId, enterprisePartner.getEnterpriseId())) {
                return result.failedBadRequest().setMessage("没有权限操作");
            }
            editParam.setEnterpriseId(enterpriseId);
            enterpriseServeMapper.put(editParam);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "异常");
        }
        return result;
    }

    public I18nResult<EnterpriseServeModel> queryOneById(String id) {
        I18nResult<EnterpriseServeModel> result = I18nResult.newInstance();
        try {
            EnterpriseServeModel enterprisePartnerModel = enterpriseServeMapper.queryById(id);
            result.succeed().setData(enterprisePartnerModel);
        } catch (Exception e) {
            log.error("查询评论发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "查询评论发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> deleteById(String id) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(enterpriseId)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            enterpriseServeMapper.deleteByIdAndEnterpriseId(id, enterpriseId);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("删除发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "删除发生异常");
        }
        return result;
    }

}
