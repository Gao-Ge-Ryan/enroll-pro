package top.gaogle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.gaogle.framework.annotation.Log;
import top.gaogle.framework.annotation.Querying;
import top.gaogle.framework.annotation.RateLimiter;
import top.gaogle.framework.annotation.RepeatSubmit;
import top.gaogle.framework.i18n.I18nResult;
import top.gaogle.framework.pojo.PageModel;
import top.gaogle.pojo.enums.BusinessTypeEnum;
import top.gaogle.pojo.enums.LimitType;
import top.gaogle.pojo.enums.OperatorTypeEnum;
import top.gaogle.pojo.model.DynamicRegisterInfoModel;
import top.gaogle.pojo.param.DynamicRegisterInfoEditParam;
import top.gaogle.pojo.param.DynamicRegisterInfoQueryParam;
import top.gaogle.service.DynamicRegisterInfoService;

import java.util.List;
import java.util.Map;

/**
 * 报名信息管理
 *
 * @author gaogle
 * @since 1.0.0
 */
@RestController
@RequestMapping("/dynamic_register_info")
public class DynamicRegisterInfoController {

    private final DynamicRegisterInfoService dynamicRegisterInfoService;

    @Autowired
    public DynamicRegisterInfoController(DynamicRegisterInfoService dynamicRegisterInfoService) {
        this.dynamicRegisterInfoService = dynamicRegisterInfoService;
    }

    /**
     * 审批通过(企业端)
     */
    @Log(title = "审批通过(企业端)", businessType = BusinessTypeEnum.UPDATE, operatorType = OperatorTypeEnum.ENTERPRISE)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_INFO_APPROVE_ENTERPRISE.value())")
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PutMapping("/approve")
    public ResponseEntity<I18nResult<Boolean>> approve(@RequestBody DynamicRegisterInfoEditParam editParam) {
        return dynamicRegisterInfoService.approve(editParam).toResponseEntity();
    }

    /**
     * 计算所有人最终成绩(企业端)
     */
    @Log(title = "计算所有人最终成绩(企业端)", businessType = BusinessTypeEnum.UPDATE, operatorType = OperatorTypeEnum.ENTERPRISE)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_INFO_SCORE_ENTERPRISE.value())")
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PutMapping("/calculate_all_final_score")
    public ResponseEntity<I18nResult<Boolean>> calculateAllFinalScore(@RequestParam String registerPublishId) {
        return dynamicRegisterInfoService.calculateAllFinalScore(registerPublishId).toResponseEntity();
    }

    /**
     * 录入笔试成绩(企业端)
     */
    @Log(title = "录入笔试成绩(企业端)", businessType = BusinessTypeEnum.UPDATE, operatorType = OperatorTypeEnum.ENTERPRISE)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_INFO_SCORE_ENTERPRISE.value())")
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PutMapping("/input_score")
    public ResponseEntity<I18nResult<Boolean>> inputScore(@RequestBody DynamicRegisterInfoEditParam editParam) {
        return dynamicRegisterInfoService.inputScore(editParam).toResponseEntity();
    }


    /**
     * 录入面试信息(企业端)
     */
    @Log(title = "录入面试信息(企业端)", businessType = BusinessTypeEnum.UPDATE, operatorType = OperatorTypeEnum.ENTERPRISE)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_INFO_SCORE_ENTERPRISE.value())")
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PutMapping("/input_interview_info")
    public ResponseEntity<I18nResult<Boolean>> inputInterviewInfo(@RequestBody DynamicRegisterInfoEditParam editParam) {
        return dynamicRegisterInfoService.inputInterviewInfo(editParam).toResponseEntity();
    }

    /**
     * 录入面试成绩(企业端)
     */
    @Log(title = "录入面试成绩(企业端)", businessType = BusinessTypeEnum.UPDATE, operatorType = OperatorTypeEnum.ENTERPRISE)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_INFO_SCORE_ENTERPRISE.value())")
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PutMapping("/input_interview_score")
    public ResponseEntity<I18nResult<Boolean>> inputInterviewScore(@RequestBody DynamicRegisterInfoEditParam editParam) {
        return dynamicRegisterInfoService.inputInterviewScore(editParam).toResponseEntity();
    }

    /**
     * 审批不通过(企业端)
     */
    @Log(title = "审批不通过(企业端)", businessType = BusinessTypeEnum.UPDATE, operatorType = OperatorTypeEnum.ENTERPRISE)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_INFO_APPROVE_ENTERPRISE.value())")
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PutMapping("/approve_not")
    public ResponseEntity<I18nResult<Boolean>> approveNot(@RequestBody DynamicRegisterInfoEditParam editParam) {
        return dynamicRegisterInfoService.approveNot(editParam).toResponseEntity();
    }

    /**
     * 撤回审批(用户端)
     */
    @Log(title = "撤回审批(用户端)", businessType = BusinessTypeEnum.UPDATE, operatorType = OperatorTypeEnum.CLIENT)
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PutMapping("/approve_revocation")
    public ResponseEntity<I18nResult<Boolean>> approveRevocation(@RequestBody DynamicRegisterInfoEditParam editParam) {
        return dynamicRegisterInfoService.approveRevocation(editParam).toResponseEntity();
    }

    /**
     * 分页条件查询(企业端)
     */
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_INFO_VIEW_ENTERPRISE.value())")
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/page")
    public ResponseEntity<I18nResult<PageModel<Map<String,Object>>>> queryByPageAndCondition(@Querying DynamicRegisterInfoQueryParam queryParam) {
        return dynamicRegisterInfoService.queryByPageAndCondition(queryParam).toResponseEntity();
    }

    /**
     * 报名申请（用户端）
     */
    @Log(title = "报名申请（用户端）", businessType = BusinessTypeEnum.INSERT, operatorType = OperatorTypeEnum.CLIENT)
    @RepeatSubmit(interval = 2000)
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PostMapping("/client_apply_info")
    public ResponseEntity<I18nResult<Boolean>> clientApplyInfo(@RequestBody DynamicRegisterInfoEditParam editParam) {
        return dynamicRegisterInfoService.clientApplyInfo(editParam).toResponseEntity();
    }

    /**
     * 报名信息修改（用户端）
     */
    @Log(title = "报名信息修改（用户端）", businessType = BusinessTypeEnum.UPDATE, operatorType = OperatorTypeEnum.CLIENT)
    @RepeatSubmit(interval = 2000)
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PutMapping("/client_update_apply_info")
    public ResponseEntity<I18nResult<Boolean>> clientUpdateApplyInfo(@RequestBody DynamicRegisterInfoEditParam editParam) {
        return dynamicRegisterInfoService.clientUpdateApplyInfo(editParam).toResponseEntity();
    }

    /**
     * 查询个人报名(用户端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/client_get_apply_info")
    public ResponseEntity<I18nResult<Map<String,Object>>> clientGetApplyInfo(@Querying DynamicRegisterInfoQueryParam queryParam) {
        return dynamicRegisterInfoService.clientGetApplyInfo(queryParam).toResponseEntity();
    }

    /**
     * 查询个人笔试成绩(用户端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/client_get_score")
    public ResponseEntity<I18nResult<DynamicRegisterInfoModel>> clientGetScore(@Querying DynamicRegisterInfoQueryParam queryParam) {
        return dynamicRegisterInfoService.clientGetScore(queryParam).toResponseEntity();
    }

    /**
     * 查询发布的报名拟录用公示信息(用户端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/client_get_offer_show")
    public ResponseEntity<I18nResult<List<DynamicRegisterInfoModel>>> clientGetOfferShow(@Querying DynamicRegisterInfoQueryParam queryParam) {
        return dynamicRegisterInfoService.clientGetOfferShow(queryParam).toResponseEntity();
    }


}
