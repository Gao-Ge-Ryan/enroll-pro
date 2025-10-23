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
import top.gaogle.pojo.model.EnterpriseSlideshowModel;
import top.gaogle.pojo.param.EnterpriseSlideshowEditParam;
import top.gaogle.pojo.param.EnterpriseSlideshowQueryParam;
import top.gaogle.service.EnterpriseSlideshowService;

import java.util.List;

/**
 * 企业轮播图管理
 *
 * @author gaogle
 * @since 1.0.0
 */
@RestController
@RequestMapping("/enterprise_slideshow")
public class EnterpriseSlideshowController {

    private final EnterpriseSlideshowService enterpriseSlideshowService;

    @Autowired
    public EnterpriseSlideshowController(EnterpriseSlideshowService enterpriseSlideshowService) {
        this.enterpriseSlideshowService = enterpriseSlideshowService;
    }


    /**
     * 添加（企业端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_SLIDESHOW_ENTERPRISE.value())")
    @PostMapping
    public ResponseEntity<I18nResult<Boolean>> insert(@RequestBody EnterpriseSlideshowEditParam editParam) {
        return enterpriseSlideshowService.insert(editParam).toResponseEntity();
    }

    /**
     * 分页条件查询（用户端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/page")
    public ResponseEntity<I18nResult<PageModel<EnterpriseSlideshowModel>>> queryByPageAndCondition(@Querying EnterpriseSlideshowQueryParam queryParam) {
        return enterpriseSlideshowService.queryByPageAndCondition(queryParam).toResponseEntity();
    }

    /**
     * 查询所有(企业端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_SLIDESHOW_ENTERPRISE.value())")
    @GetMapping("/enterprise/all")
    public ResponseEntity<I18nResult<List<EnterpriseSlideshowModel>>> queryAll() {
        return enterpriseSlideshowService.queryAll().toResponseEntity();
    }

    /**
     * 查询所有(用户端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/client/all")
    public ResponseEntity<I18nResult<List<EnterpriseSlideshowModel>>> clientQueryAll(@RequestParam String enterpriseId) {
        return enterpriseSlideshowService.clientQueryAll(enterpriseId).toResponseEntity();
    }

    /**
     * 修改（企业端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_SLIDESHOW_ENTERPRISE.value())")
    @PutMapping
    public ResponseEntity<I18nResult<Boolean>> put(@RequestBody EnterpriseSlideshowEditParam editParam) {
        return enterpriseSlideshowService.put(editParam).toResponseEntity();
    }

    /**
     * 根据id查询详情（用户端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/{id}")
    public ResponseEntity<I18nResult<EnterpriseSlideshowModel>> queryOneById(@PathVariable("id") String id) {
        return enterpriseSlideshowService.queryOneById(id).toResponseEntity();
    }

    /**
     * 根据id删除(企业端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_SLIDESHOW_ENTERPRISE.value())")
    @DeleteMapping("/{id}")
    public ResponseEntity<I18nResult<Boolean>> deleteById(@PathVariable("id") String id) {
        return enterpriseSlideshowService.deleteById(id).toResponseEntity();
    }

}
