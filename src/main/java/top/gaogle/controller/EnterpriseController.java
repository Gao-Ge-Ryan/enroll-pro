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
import top.gaogle.pojo.dto.ApproveDTO;
import top.gaogle.pojo.dto.PutUserRoleDTO;
import top.gaogle.pojo.enums.BusinessTypeEnum;
import top.gaogle.pojo.enums.EnterpriseShowStatusEnum;
import top.gaogle.pojo.enums.LimitType;
import top.gaogle.pojo.enums.OperatorTypeEnum;
import top.gaogle.pojo.model.EnterpriseModel;
import top.gaogle.pojo.model.EnterpriseUserModel;
import top.gaogle.pojo.model.RoleModel;
import top.gaogle.pojo.param.*;
import top.gaogle.service.EnterpriseService;

import java.util.List;

/**
 * 企业管理
 *
 * @author gaogle
 * @since 1.0.0
 */
@RestController
@RequestMapping("/enterprise")
public class EnterpriseController {
    private final EnterpriseService enterpriseService;

    @Autowired
    public EnterpriseController(EnterpriseService enterpriseService) {
        this.enterpriseService = enterpriseService;
    }


    /**
     * 申请企业(用户端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PostMapping("/apply")
    public ResponseEntity<I18nResult<Boolean>> apply(@RequestBody EnterpriseEditParam editParam) {
        return enterpriseService.apply(editParam).toResponseEntity();
    }

    /**
     * 审批企业通过(管理端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_APPROVE_ADMIN.value())")
    @PutMapping("/approve/{enterpriseId}")
    public ResponseEntity<I18nResult<Boolean>> approve(@PathVariable("enterpriseId") String enterpriseId) {
        return enterpriseService.approve(enterpriseId).toResponseEntity();
    }

    /**
     * 审批企业不通过(管理端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_APPROVE_ADMIN.value())")
    @PutMapping("/approve_not/{enterpriseId}")
    public ResponseEntity<I18nResult<Boolean>> approveNot(@PathVariable("enterpriseId") String enterpriseId, @RequestBody ApproveDTO approveDTO) {
        return enterpriseService.approveNot(enterpriseId, approveDTO).toResponseEntity();
    }

    /**
     * 管理企业是否在客户端列表展示(管理端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_APPROVE_ADMIN.value())")
    @PutMapping("/put_show_status/{enterpriseId}")
    public ResponseEntity<I18nResult<Boolean>> putShowStatus(@PathVariable("enterpriseId") String enterpriseId, @RequestParam("showStatus") EnterpriseShowStatusEnum showStatus) {
        return enterpriseService.putShowStatus(enterpriseId, showStatus).toResponseEntity();
    }

//    /**
//     * 撤回审批企业(用户端)
//     */
//    @PutMapping("/approve_revocation")
//    public ResponseEntity<I18nResult<Boolean>> approveRevocation() {
//        return enterpriseService.approveRevocation().toResponseEntity();
//    }

//    /**
//     * 申请审批企业信息修改(用户端)
//     */
//    @PutMapping("/approve_info_update")
//    public ResponseEntity<I18nResult<Boolean>> approveInfoUpdate(@RequestBody ApproveDTO approveDTO) {
//        return enterpriseService.approveInfoUpdate(approveDTO).toResponseEntity();
//    }

//    /**
//     * 删除审批企业信息(用户端)
//     */
//    @DeleteMapping("/client_delete_approve")
//    public ResponseEntity<I18nResult<Boolean>> clientDeleteApproveInfo() {
//        return enterpriseService.clientDeleteApproveInfo().toResponseEntity();
//    }

    /**
     * 查询审批企业信息(用户端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/client_query_approve")
    public ResponseEntity<I18nResult<ApproveDTO>> clientQueryApprove() {
        return enterpriseService.clientQueryApprove().toResponseEntity();
    }

//    /**
//     * 添加企业（管理端）
//     */
//    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_INSERT_ADMIN.value())")
//    @PostMapping
//    public ResponseEntity<I18nResult<Boolean>> insert(@RequestBody EnterpriseEditParam editParam) {
//        return enterpriseService.insert(editParam).toResponseEntity();
//    }

//    /**
//     * 修改企业(管理员)
//     */
//    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_PUT_ADMIN.value())")
//    @PutMapping
//    public ResponseEntity<I18nResult<Boolean>> putEnterprise(@RequestBody EnterpriseEditParam editParam) {
//        return enterpriseService.putEnterprise(editParam).toResponseEntity();
//    }

    /**
     * 添加企业员工(企业端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_USER_ENTERPRISE.value())")
    @PostMapping("/add_user")
    public ResponseEntity<I18nResult<Boolean>> addUser(@RequestBody EnterpriseUserEditParam editParam) {
        return enterpriseService.addUser(editParam).toResponseEntity();
    }

    /**
     * 删除企业员工（企业端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_USER_ENTERPRISE.value())")
    @DeleteMapping("/delete_user")
    public ResponseEntity<I18nResult<Boolean>> deleteUser(@RequestParam String accountBy) {
        return enterpriseService.deleteUser(accountBy).toResponseEntity();
    }

    /**
     * 修改企业员工角色（企业端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @Log(title = "修改企业员工角色", businessType = BusinessTypeEnum.UPDATE, operatorType = OperatorTypeEnum.ADMIN)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_USER_ENTERPRISE.value())")
    @PutMapping("/put_role")
    public ResponseEntity<I18nResult<Boolean>> putUserRole(@RequestBody PutUserRoleDTO putUserRoleDTO) {
        return enterpriseService.putUserRole(putUserRoleDTO).toResponseEntity();
    }

    /**
     * 修改企业信息（企业端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_PUT_ENTERPRISE.value())")
    @PutMapping("/enterprise_put")
    public ResponseEntity<I18nResult<Boolean>> enterprisePutEnterprise(@RequestBody EnterpriseEditParam editParam) {
        return enterpriseService.enterprisePutEnterprise(editParam).toResponseEntity();
    }

    /**
     * 修改企业支付宝支付密钥（企业端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_ALIPAY_SECRET_ENTERPRISE.value())")
    @PutMapping("/enterprise_alipay_secret")
    public ResponseEntity<I18nResult<Boolean>> enterpriseAlipaySecret(@RequestBody AlipaySecretEditParam editParam) {
        return enterpriseService.enterpriseAlipaySecret(editParam).toResponseEntity();
    }

    /**
     * 查询企业信息（用户端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/client_query")
    public ResponseEntity<I18nResult<EnterpriseModel>> clientQueryEnterprise(@RequestParam String enterpriseId) {
        return enterpriseService.clientQueryEnterprise(enterpriseId).toResponseEntity();
    }

    /**
     * 查询企业信息（企业端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_VIEW_ENTERPRISE.value())")
    @GetMapping("/enterprise_query")
    public ResponseEntity<I18nResult<EnterpriseModel>> enterpriseQueryEnterprise() {
        return enterpriseService.enterpriseQueryEnterprise().toResponseEntity();
    }

    /**
     * 查询企业下所有角色（企业端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_USER_ENTERPRISE.value())")
    @GetMapping("/enterprise_all_role")
    public ResponseEntity<I18nResult<List<RoleModel>>> enterpriseAllRole() {
        return enterpriseService.enterpriseAllRole().toResponseEntity();
    }

    /**
     * 获取企业余额（企业端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_VIEW_ENTERPRISE.value())")
    @GetMapping("/enterprise_query_balance")
    public ResponseEntity<I18nResult<String>> enterpriseQueryEnterpriseBalance() {
        return enterpriseService.enterpriseQueryEnterpriseBalance().toResponseEntity();
    }

    /**
     * 分页查询企业（用户端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @Anonymous
    @GetMapping("/client_query_page")
    public ResponseEntity<I18nResult<PageModel<EnterpriseModel>>> clientQueryByPage(@Querying EnterpriseQueryParam queryParam) {
        return enterpriseService.clientQueryByPage(queryParam).toResponseEntity();
    }

    /**
     * 分页条件查询企业下账户(企业端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_USER_ENTERPRISE.value())")
    @GetMapping("/enterprise_user_page")
    public ResponseEntity<I18nResult<PageModel<EnterpriseUserModel>>> queryEnterpriseUserByPageAndCondition(@Querying EnterpriseUserQueryParam queryParam) {
        return enterpriseService.queryEnterpriseUserByPageAndCondition(queryParam).toResponseEntity();
    }

    /**
     * 分页条件查询（管理端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_VIEW_ADMIN.value())")
    @GetMapping("/page")
    public ResponseEntity<I18nResult<PageModel<EnterpriseModel>>> queryByPageAndCondition(@Querying EnterpriseQueryParam queryParam) {
        return enterpriseService.queryByPageAndCondition(queryParam).toResponseEntity();
    }

    /**
     * 根据id查询详情（管理端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_VIEW_ADMIN.value())")
    @GetMapping("/{id}")
    public ResponseEntity<I18nResult<EnterpriseModel>> queryOneById(@PathVariable("id") String id) {
        return enterpriseService.queryOneById(id).toResponseEntity();
    }

    /**
     * 根据id删除企业（管理端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @Log(title = "根据id删除企业（管理端）", businessType = BusinessTypeEnum.DELETE, operatorType = OperatorTypeEnum.ADMIN)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_DELETE_ADMIN.value())")
    @DeleteMapping("/{id}")
    public ResponseEntity<I18nResult<EnterpriseModel>> deleteById(@PathVariable("id") String id) {
        return enterpriseService.deleteById(id).toResponseEntity();
    }
}
