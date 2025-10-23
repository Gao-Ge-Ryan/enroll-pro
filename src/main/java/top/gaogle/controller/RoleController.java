package top.gaogle.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.gaogle.framework.annotation.Log;
import top.gaogle.framework.annotation.Querying;
import top.gaogle.framework.annotation.RateLimiter;
import top.gaogle.framework.i18n.I18nResult;
import top.gaogle.framework.pojo.PageModel;
import top.gaogle.pojo.enums.BusinessTypeEnum;
import top.gaogle.pojo.enums.LimitType;
import top.gaogle.pojo.enums.OperatorTypeEnum;
import top.gaogle.pojo.model.RoleModel;
import top.gaogle.pojo.param.RoleEditParam;
import top.gaogle.pojo.param.RoleQueryParam;
import top.gaogle.service.RoleService;

import java.util.List;

/**
 * 角色管理
 *
 * @author gaogle
 * @since 1.0.0
 */
@RestController
@RequestMapping("/role")
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }


    /**
     * 添加角色（管理端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @Log(title = "添加角色", businessType = BusinessTypeEnum.INSERT, operatorType = OperatorTypeEnum.ADMIN)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ROLE_INSERT_ADMIN.value())")
    @PostMapping
    public ResponseEntity<I18nResult<Boolean>> insertRole(@RequestBody RoleEditParam editParam) {
        return roleService.insertRole(editParam).toResponseEntity();
    }

    /**
     * 修改角色（管理端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @Log(title = "修改角色", businessType = BusinessTypeEnum.UPDATE, operatorType = OperatorTypeEnum.ADMIN)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ROLE_PUT_ADMIN.value())")
    @PutMapping
    public ResponseEntity<I18nResult<Boolean>> patchRole(@RequestBody RoleEditParam editParam) {
        return roleService.patchRole(editParam).toResponseEntity();
    }

    /**
     * 删除角色（管理端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @Log(title = "删除角色", businessType = BusinessTypeEnum.DELETE, operatorType = OperatorTypeEnum.ADMIN)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ROLE_DELETE_ADMIN.value())")
    @DeleteMapping("/{role_id}")
    public ResponseEntity<I18nResult<Boolean>> deleteRole(@PathVariable("role_id") String roleId) {
        return roleService.deleteRole(roleId).toResponseEntity();
    }

    /**
     * 分页条件查询（管理端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ROLE_VIEW_ADMIN.value())")
    @GetMapping("/page")
    public ResponseEntity<I18nResult<PageModel<RoleModel>>> queryByPageAndCondition(@Querying RoleQueryParam queryParam) {
        return roleService.queryByPageAndCondition(queryParam).toResponseEntity();
    }

    /**
     * 根据用户account查询角色（管理端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ROLE_VIEW_ADMIN.value())")
    @GetMapping("/account/{account_by}")
    public ResponseEntity<I18nResult<List<RoleModel>>> queryRolesByUserId(@PathVariable("account_by") String accountBy) {
        return roleService.queryRolesByUserId(accountBy).toResponseEntity();
    }

    /**
     * 查询所有角色（管理端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ROLE_VIEW_ADMIN.value())")
    @GetMapping("/all")
    public ResponseEntity<I18nResult<List<RoleModel>>> queryAll() {
        return roleService.queryAll().toResponseEntity();
    }

    /**
     * 根据角色id查详情（管理端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ROLE_VIEW_ADMIN.value())")
    @GetMapping("/{role_id}")
    public ResponseEntity<I18nResult<RoleModel>> queryDetailByRoleId(@PathVariable("role_id") String roleId) {
        return roleService.queryDetailByRoleId(roleId).toResponseEntity();
    }


}
