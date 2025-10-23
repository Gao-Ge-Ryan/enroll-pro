package top.gaogle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.gaogle.framework.annotation.Querying;
import top.gaogle.framework.annotation.RateLimiter;
import top.gaogle.framework.i18n.I18nResult;
import top.gaogle.framework.pojo.PageModel;
import top.gaogle.pojo.enums.LimitType;
import top.gaogle.pojo.model.EnterprisePartnerModel;
import top.gaogle.pojo.param.EnterprisePartnerEditParam;
import top.gaogle.pojo.param.EnterprisePartnerQueryParam;
import top.gaogle.service.EnterprisePartnerService;

import java.util.List;

/**
 * 企业伙伴管理
 *
 * @author gaogle
 * @since 1.0.0
 */
@RestController
@RequestMapping("/enterprise_partner")
public class EnterprisePartnerController {

    private final EnterprisePartnerService enterprisePartnerService;

    @Autowired
    public EnterprisePartnerController(EnterprisePartnerService enterprisePartnerService) {
        this.enterprisePartnerService = enterprisePartnerService;
    }

    /**
     * 查询所有(用户端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/client/all")
    public ResponseEntity<I18nResult<List<EnterprisePartnerModel>>> clientQueryAll(@RequestParam String enterpriseId) {
        return enterprisePartnerService.clientQueryAll(enterpriseId).toResponseEntity();
    }


    /**
     * 添加（企业端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_PARTNER_ENTERPRISE.value())")
    @PostMapping
    public ResponseEntity<I18nResult<Boolean>> insert(@RequestBody EnterprisePartnerEditParam editParam) {
        return enterprisePartnerService.insert(editParam).toResponseEntity();
    }

    /**
     * 分页条件查询(企业端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/page")
    public ResponseEntity<I18nResult<PageModel<EnterprisePartnerModel>>> queryByPageAndCondition(@Querying EnterprisePartnerQueryParam queryParam) {
        return enterprisePartnerService.queryByPageAndCondition(queryParam).toResponseEntity();
    }

    /**
     * 修改（企业端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_PARTNER_ENTERPRISE.value())")
    @PutMapping
    public ResponseEntity<I18nResult<Boolean>> put(@RequestBody EnterprisePartnerEditParam editParam) {
        return enterprisePartnerService.put(editParam).toResponseEntity();
    }

    /**
     * 根据id查询详情（用户端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/{id}")
    public ResponseEntity<I18nResult<EnterprisePartnerModel>> queryOneById(@PathVariable("id") String id) {
        return enterprisePartnerService.queryOneById(id).toResponseEntity();
    }

    /**
     * 根据id删除(企业端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_PARTNER_ENTERPRISE.value())")
    @DeleteMapping("/{id}")
    public ResponseEntity<I18nResult<Boolean>> deleteById(@PathVariable("id") String id) {
        return enterprisePartnerService.deleteById(id).toResponseEntity();
    }
}
