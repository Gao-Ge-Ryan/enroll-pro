package top.gaogle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.gaogle.framework.annotation.Anonymous;
import top.gaogle.framework.annotation.Log;
import top.gaogle.framework.annotation.Querying;
import top.gaogle.framework.annotation.RateLimiter;
import top.gaogle.framework.i18n.I18nResult;
import top.gaogle.framework.pojo.PageModel;
import top.gaogle.pojo.dto.OfferPutDTO;
import top.gaogle.pojo.dto.UserRegisterDTO;
import top.gaogle.pojo.enums.BusinessTypeEnum;
import top.gaogle.pojo.enums.LimitType;
import top.gaogle.pojo.enums.OperatorTypeEnum;
import top.gaogle.pojo.model.ActivityInfoModel;
import top.gaogle.pojo.model.RegisterPublishModel;
import top.gaogle.pojo.param.RegisterPublishEditParam;
import top.gaogle.pojo.param.RegisterPublishQueryParam;
import top.gaogle.service.RegisterPublishService;

import java.util.List;

/**
 * 报名发布
 *
 * @author gaogle
 * @since 1.0.0
 */
@RestController
@RequestMapping("/register_publish")
public class RegisterPublishController {
    private final RegisterPublishService registerPublishService;

    @Autowired
    public RegisterPublishController(RegisterPublishService registerPublishService) {
        this.registerPublishService = registerPublishService;
    }

    /**
     * 新增(企业端)
     */
    @Log(title = "报名发布新增(企业端)", businessType = BusinessTypeEnum.INSERT, operatorType = OperatorTypeEnum.ENTERPRISE)
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_PUBLISH_INSERT_ENTERPRISE.value())")
    @PostMapping
    public ResponseEntity<I18nResult<Boolean>> add(@RequestBody RegisterPublishEditParam editParam) {
        return registerPublishService.add(editParam).toResponseEntity();
    }

    /**
     * 分页条件查询企业发布得考试(企业端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_PUBLISH_VIEW_ENTERPRISE.value())")
    @GetMapping("/enterprise/page")
    public ResponseEntity<I18nResult<PageModel<RegisterPublishModel>>> enterpriseQueryByPageAndCondition(@Querying RegisterPublishQueryParam queryParam) {
        return registerPublishService.enterpriseQueryByPageAndCondition(queryParam).toResponseEntity();
    }

    /**
     * 分页条件查询企业发布得考试(客户端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @Anonymous
    @GetMapping("/client/page")
    public ResponseEntity<I18nResult<PageModel<RegisterPublishModel>>> clientQueryByPageAndCondition(@Querying RegisterPublishQueryParam queryParam) {
        return registerPublishService.clientQueryByPageAndCondition(queryParam).toResponseEntity();
    }

    /**
     * 分页条件查询企业发布得考试拟录用公示(客户端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @Anonymous
    @GetMapping("/client/page/offer_show")
    public ResponseEntity<I18nResult<PageModel<RegisterPublishModel>>> clientQueryOfferShowByPageAndCondition(@Querying RegisterPublishQueryParam queryParam) {
        return registerPublishService.clientQueryOfferShowByPageAndCondition(queryParam).toResponseEntity();
    }

    /**
     * 分页条件查询报名的考试(客户端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/client/register/page")
    public ResponseEntity<I18nResult<PageModel<UserRegisterDTO>>> clientQueryRegisterByPageAndCondition(@Querying RegisterPublishQueryParam queryParam) {
        return registerPublishService.clientQueryRegisterByPageAndCondition(queryParam).toResponseEntity();
    }

    /**
     * 查询报名下考试场次信息(企业端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/enterprise/activity_info")
    public ResponseEntity<I18nResult<List<ActivityInfoModel>>> enterpriseQueryActivityInfo(@RequestParam String registerPublishId) {
        return registerPublishService.enterpriseQueryActivityInfo(registerPublishId).toResponseEntity();
    }

    /**
     * 报名发布修改（企业端）
     */
    @Log(title = "报名发布修改（企业端）", businessType = BusinessTypeEnum.UPDATE, operatorType = OperatorTypeEnum.ENTERPRISE)
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_PUBLISH_PUT_ENTERPRISE.value())")
    @PutMapping
    public ResponseEntity<I18nResult<Boolean>> enterprisePut(@RequestBody RegisterPublishEditParam editParam) {
        return registerPublishService.enterprisePut(editParam).toResponseEntity();
    }

    /**
     * 录用情况展示标志和说明修改（企业端）
     */
    @Log(title = "录用情况展示标志和说明修改（企业端）", businessType = BusinessTypeEnum.UPDATE, operatorType = OperatorTypeEnum.ENTERPRISE)
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_INFO_SCORE_ENTERPRISE.value())")
    @PutMapping("/offer_put")
    public ResponseEntity<I18nResult<Boolean>> offerPut(@RequestBody OfferPutDTO offerPutDTO) {
        return registerPublishService.offerPut(offerPutDTO).toResponseEntity();
    }

    /**
     * 分配考点（企业端）
     */
    @Log(title = "分配考点（企业端）", businessType = BusinessTypeEnum.UPDATE, operatorType = OperatorTypeEnum.ENTERPRISE)
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_SPOT_ENTERPRISE.value())")
    @PutMapping("/enterprise_allocate_spot")
    public ResponseEntity<I18nResult<Boolean>> enterpriseAllocateSpot(@RequestBody RegisterPublishEditParam editParam) {
        return registerPublishService.enterpriseAllocateSpot(editParam).toResponseEntity();
    }

    /**
     * 分页条件查询（管理端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_PUBLISH_VIEW_ADMIN.value())")
    @GetMapping("/page")
    public ResponseEntity<I18nResult<PageModel<RegisterPublishModel>>> queryByPageAndCondition(@Querying RegisterPublishQueryParam queryParam) {
        return registerPublishService.queryByPageAndCondition(queryParam).toResponseEntity();
    }

    /**
     * 根据id查询详情(用户端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/{id}")
    public ResponseEntity<I18nResult<RegisterPublishModel>> queryOneById(@PathVariable("id") String id) {
        return registerPublishService.queryOneById(id).toResponseEntity();
    }

    /**
     * 根据id删除(企业端)
     */
    @Log(title = "报名发布根据id删除(企业端)", businessType = BusinessTypeEnum.DELETE, operatorType = OperatorTypeEnum.ENTERPRISE)
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_PUBLISH_DELETE_ENTERPRISE.value())")
    @DeleteMapping("/{id}")
    public ResponseEntity<I18nResult<Boolean>> deleteById(@PathVariable("id") String id) {
        return registerPublishService.deleteById(id).toResponseEntity();
    }

}
