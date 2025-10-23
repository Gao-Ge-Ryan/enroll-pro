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
import top.gaogle.pojo.model.EnterpriseServeModel;
import top.gaogle.pojo.param.EnterpriseServeEditParam;
import top.gaogle.pojo.param.EnterpriseServeQueryParam;
import top.gaogle.service.EnterpriseServeService;

import java.util.List;

/**
 * 企业服务管理
 *
 * @author gaogle
 * @since 1.0.0
 */
@RestController
@RequestMapping("/enterprise_serve")
public class EnterpriseServeController {

    private final EnterpriseServeService enterpriseServeService;

    @Autowired
    public EnterpriseServeController(EnterpriseServeService enterpriseServeService) {
        this.enterpriseServeService = enterpriseServeService;
    }

    /**
     * 查询所有(用户端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/client/all")
    public ResponseEntity<I18nResult<List<EnterpriseServeModel>>> clientQueryAll(@RequestParam String enterpriseId) {
        return enterpriseServeService.clientQueryAll(enterpriseId).toResponseEntity();
    }


    /**
     * 添加（企业端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_SERVE_ENTERPRISE.value())")
    @PostMapping
    public ResponseEntity<I18nResult<Boolean>> insert(@RequestBody EnterpriseServeEditParam editParam) {
        return enterpriseServeService.insert(editParam).toResponseEntity();
    }

    /**
     * 分页条件查询(企业端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_SERVE_ENTERPRISE.value())")
    @GetMapping("/page")
    public ResponseEntity<I18nResult<PageModel<EnterpriseServeModel>>> queryByPageAndCondition(@Querying EnterpriseServeQueryParam queryParam) {
        return enterpriseServeService.queryByPageAndCondition(queryParam).toResponseEntity();
    }

    /**
     * 修改（企业端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_SERVE_ENTERPRISE.value())")
    @PutMapping
    public ResponseEntity<I18nResult<Boolean>> put(@RequestBody EnterpriseServeEditParam editParam) {
        return enterpriseServeService.put(editParam).toResponseEntity();
    }

    /**
     * 根据id查询详情（用户端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/{id}")
    public ResponseEntity<I18nResult<EnterpriseServeModel>> queryOneById(@PathVariable("id") String id) {
        return enterpriseServeService.queryOneById(id).toResponseEntity();
    }

    /**
     * 根据id删除(企业端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_SERVE_ENTERPRISE.value())")
    @DeleteMapping("/{id}")
    public ResponseEntity<I18nResult<Boolean>> deleteById(@PathVariable("id") String id) {
        return enterpriseServeService.deleteById(id).toResponseEntity();
    }

}
