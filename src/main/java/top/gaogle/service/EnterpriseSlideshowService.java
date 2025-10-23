package top.gaogle.service;

import com.github.pagehelper.page.PageMethod;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.gaogle.dao.master.EnterpriseSlideshowMapper;
import top.gaogle.framework.i18n.I18ResultCode;
import top.gaogle.framework.i18n.I18nResult;
import top.gaogle.framework.pojo.PageModel;
import top.gaogle.framework.util.DateUtil;
import top.gaogle.framework.util.SecurityUtil;
import top.gaogle.framework.util.UniqueUtil;
import top.gaogle.pojo.enums.HttpStatusEnum;
import top.gaogle.pojo.model.EnterpriseSlideshowModel;
import top.gaogle.pojo.param.EnterpriseSlideshowEditParam;
import top.gaogle.pojo.param.EnterpriseSlideshowQueryParam;

import java.util.List;
import java.util.Objects;

@Service
public class EnterpriseSlideshowService extends SuperService {

    private final EnterpriseSlideshowMapper enterpriseSlideshowMapper;

    @Autowired
    public EnterpriseSlideshowService(EnterpriseSlideshowMapper enterpriseSlideshowMapper) {
        this.enterpriseSlideshowMapper = enterpriseSlideshowMapper;
    }

    public I18nResult<Boolean> insert(EnterpriseSlideshowEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            String loginUsername = SecurityUtil.getLoginUsername();
            String picture = editParam.getPicture();
            if (StringUtils.isAnyEmpty(picture, enterpriseId)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage(I18ResultCode.MESSAGE, "缺少必要参数");
            }
            editParam.setEnterpriseId(enterpriseId);
            editParam.setId(UniqueUtil.getUniqueId());
            Long timeMillis = DateUtil.currentTimeMillis();
            editParam.setCreateAt(timeMillis);
            editParam.setUpdateAt(timeMillis);
            editParam.setCreateBy(loginUsername);
            editParam.setUpdateBy(loginUsername);
            enterpriseSlideshowMapper.insert(editParam);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("添加发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "添加发生异常");
        }
        return result;
    }

    public I18nResult<PageModel<EnterpriseSlideshowModel>> queryByPageAndCondition(EnterpriseSlideshowQueryParam queryParam) {
        I18nResult<PageModel<EnterpriseSlideshowModel>> result = I18nResult.newInstance();
        try {
            PageMethod.startPage(queryParam.getPageNum(), queryParam.getPageSize());
            List<EnterpriseSlideshowModel> commentModels = enterpriseSlideshowMapper.queryByPageAndCondition(queryParam);
            PageModel<EnterpriseSlideshowModel> pageModel = new PageModel<>(commentModels);
            result.succeed().setData(pageModel);
        } catch (Exception e) {
            log.error("分页条件查询发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "分页条件查询发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> put(EnterpriseSlideshowEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String id = editParam.getId();
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(id, enterpriseId)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            EnterpriseSlideshowModel enterprisePartner = enterpriseSlideshowMapper.queryById(id);
            if (enterprisePartner == null) {
                return result.failedBadRequest().setMessage("实体不存在");
            }
            if (!Objects.equals(enterpriseId, enterprisePartner.getEnterpriseId())) {
                return result.failedBadRequest().setMessage("没有权限操作");
            }
            editParam.setEnterpriseId(enterpriseId);
            enterpriseSlideshowMapper.put(editParam);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "异常");
        }
        return result;
    }

    public I18nResult<EnterpriseSlideshowModel> queryOneById(String id) {
        I18nResult<EnterpriseSlideshowModel> result = I18nResult.newInstance();
        try {
            EnterpriseSlideshowModel enterprisePartnerModel = enterpriseSlideshowMapper.queryById(id);
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
            enterpriseSlideshowMapper.deleteByIdAndEnterpriseId(id, enterpriseId);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("删除发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "删除发生异常");
        }
        return result;
    }

    public I18nResult<List<EnterpriseSlideshowModel>> queryAll() {
        I18nResult<List<EnterpriseSlideshowModel>> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(enterpriseId)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            List<EnterpriseSlideshowModel> slideshowModels = enterpriseSlideshowMapper.queryByEnterpriseId(enterpriseId);
            result.succeed().setData(slideshowModels);
        } catch (Exception e) {
            log.error("分页条件查询发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "分页条件查询发生异常");
        }
        return result;
    }

    public I18nResult<List<EnterpriseSlideshowModel>> clientQueryAll(String enterpriseId) {

        I18nResult<List<EnterpriseSlideshowModel>> result = I18nResult.newInstance();
        try {
            List<EnterpriseSlideshowModel> slideshowModels = enterpriseSlideshowMapper.queryByEnterpriseId(enterpriseId);
            result.succeed().setData(slideshowModels);
        } catch (Exception e) {
            log.error("分页条件查询发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "查询发生异常");
        }
        return result;
    }
}
