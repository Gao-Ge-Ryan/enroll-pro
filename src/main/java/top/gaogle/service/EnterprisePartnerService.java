package top.gaogle.service;

import com.github.pagehelper.page.PageMethod;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.gaogle.dao.master.EnterprisePartnerMapper;
import top.gaogle.framework.i18n.I18ResultCode;
import top.gaogle.framework.i18n.I18nResult;
import top.gaogle.framework.pojo.PageModel;
import top.gaogle.framework.util.DateUtil;
import top.gaogle.framework.util.SecurityUtil;
import top.gaogle.framework.util.UniqueUtil;
import top.gaogle.pojo.enums.HttpStatusEnum;
import top.gaogle.pojo.model.EnterprisePartnerModel;
import top.gaogle.pojo.param.EnterprisePartnerEditParam;
import top.gaogle.pojo.param.EnterprisePartnerQueryParam;

import java.util.List;
import java.util.Objects;

@Service
public class EnterprisePartnerService extends SuperService {

    private final EnterprisePartnerMapper enterprisePartnerMapper;

    @Autowired
    public EnterprisePartnerService(EnterprisePartnerMapper enterprisePartnerMapper) {
        this.enterprisePartnerMapper = enterprisePartnerMapper;
    }

    public I18nResult<List<EnterprisePartnerModel>> clientQueryAll(String enterpriseId) {

        I18nResult<List<EnterprisePartnerModel>> result = I18nResult.newInstance();
        try {
            List<EnterprisePartnerModel> models = enterprisePartnerMapper.queryByEnterpriseId(enterpriseId);
            result.succeed().setData(models);
        } catch (Exception e) {
            log.error("查询发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "查询发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> insert(EnterprisePartnerEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            String loginUsername = SecurityUtil.getLoginUsername();
            String logo = editParam.getLogo();
            if (StringUtils.isAnyEmpty(logo, enterpriseId)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage(I18ResultCode.MESSAGE, "缺少必要参数");
            }
            editParam.setEnterpriseId(enterpriseId);
            editParam.setId(UniqueUtil.getUniqueId());
            Long timeMillis = DateUtil.currentTimeMillis();
            editParam.setCreateAt(timeMillis);
            editParam.setUpdateAt(timeMillis);
            editParam.setCreateBy(loginUsername);
            editParam.setUpdateBy(loginUsername);
            enterprisePartnerMapper.insert(editParam);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("添加发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "添加发生异常");
        }
        return result;
    }

    public I18nResult<PageModel<EnterprisePartnerModel>> queryByPageAndCondition(EnterprisePartnerQueryParam queryParam) {
        I18nResult<PageModel<EnterprisePartnerModel>> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(enterpriseId)){
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            queryParam.setEnterpriseId(enterpriseId);
            PageMethod.startPage(queryParam.getPageNum(), queryParam.getPageSize());
            List<EnterprisePartnerModel> commentModels = enterprisePartnerMapper.queryByPageAndCondition(queryParam);
            PageModel<EnterprisePartnerModel> pageModel = new PageModel<>(commentModels);
            result.succeed().setData(pageModel);
        } catch (Exception e) {
            log.error("分页条件查询发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "分页条件查询发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> put(EnterprisePartnerEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String id = editParam.getId();
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(id, enterpriseId)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            EnterprisePartnerModel enterprisePartner = enterprisePartnerMapper.queryById(id);
            if (enterprisePartner == null) {
                return result.failedBadRequest().setMessage("实体不存在");
            }
            if (!Objects.equals(enterpriseId, enterprisePartner.getEnterpriseId())) {
                return result.failedBadRequest().setMessage("没有权限操作");
            }
            editParam.setEnterpriseId(enterpriseId);
            enterprisePartnerMapper.put(editParam);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "异常");
        }
        return result;
    }

    public I18nResult<EnterprisePartnerModel> queryOneById(String id) {
        I18nResult<EnterprisePartnerModel> result = I18nResult.newInstance();
        try {
            EnterprisePartnerModel enterprisePartnerModel = enterprisePartnerMapper.queryById(id);
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
            enterprisePartnerMapper.deleteByIdAndEnterpriseId(id, enterpriseId);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("删除发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "删除发生异常");
        }
        return result;
    }
}
