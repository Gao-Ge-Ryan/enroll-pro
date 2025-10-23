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
import top.gaogle.pojo.model.SpotInfoModel;
import top.gaogle.pojo.param.SpotInfoEditParam;
import top.gaogle.pojo.param.SpotInfoQueryParam;
import top.gaogle.service.SpotInfoService;

import java.util.List;

/**
 * 考点模板管理
 *
 * @author gaogle
 * @since 1.0.0
 */
@RestController
@RequestMapping("/spot_info")
public class SpotInfoController {

    private final SpotInfoService spotInfoService;

    @Autowired
    public SpotInfoController(SpotInfoService spotInfoService) {
        this.spotInfoService = spotInfoService;
    }

    /**
     * 添加考点(企业端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_SPOT_ENTERPRISE.value())")
    @PostMapping
    public ResponseEntity<I18nResult<Boolean>> insert(@RequestBody SpotInfoEditParam editParam) {
        return spotInfoService.insert(editParam).toResponseEntity();
    }

    /**
     * 修改考点(企业端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_SPOT_ENTERPRISE.value())")
    @PutMapping
    public ResponseEntity<I18nResult<Boolean>> putSpotInfo(@RequestBody SpotInfoEditParam editParam) {
        return spotInfoService.putSpotInfo(editParam).toResponseEntity();
    }

    /**
     * 分页条件查询（企业端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_SPOT_ENTERPRISE.value())")
    @GetMapping("/page")
    public ResponseEntity<I18nResult<PageModel<SpotInfoModel>>> queryByPageAndCondition(@Querying SpotInfoQueryParam queryParam) {
        return spotInfoService.queryByPageAndCondition(queryParam).toResponseEntity();
    }

    /**
     * 查询企业下所有可用考点（企业端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_SPOT_ENTERPRISE.value())")
    @GetMapping("/enterprise/all")
    public ResponseEntity<I18nResult<List<SpotInfoModel>>> enterpriseEnableAll() {
        return spotInfoService.enterpriseEnableAll().toResponseEntity();
    }

    /**
     * 根据id查询详情（企业端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_SPOT_ENTERPRISE.value())")
    @GetMapping("/{id}")
    public ResponseEntity<I18nResult<SpotInfoModel>> queryOneById(@PathVariable("id") String id) {
        return spotInfoService.queryOneById(id).toResponseEntity();
    }


    /**
     * 根据id删除（企业端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_SPOT_ENTERPRISE.value())")
    @DeleteMapping("/{id}")
    public ResponseEntity<I18nResult<Boolean>> deleteById(@PathVariable("id") String id) {
        return spotInfoService.deleteById(id).toResponseEntity();
    }

}
