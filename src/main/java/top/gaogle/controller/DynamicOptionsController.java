package top.gaogle.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.gaogle.framework.annotation.Querying;
import top.gaogle.framework.annotation.RateLimiter;
import top.gaogle.framework.i18n.I18nResult;
import top.gaogle.pojo.enums.LimitType;
import top.gaogle.pojo.model.DynamicOptionsModel;
import top.gaogle.pojo.param.DynamicOptionsEditParam;
import top.gaogle.pojo.param.DynamicOptionsQueryParam;
import top.gaogle.service.DynamicOptionsService;

import java.util.List;

/**
 * 动态选项
 *
 * @author gaogle
 * @since 1.0.0
 */
@RestController
@RequestMapping("/dynamic_options")
public class DynamicOptionsController {

    private final DynamicOptionsService dynamicOptionsService;

    public DynamicOptionsController(DynamicOptionsService dynamicOptionsService) {
        this.dynamicOptionsService = dynamicOptionsService;
    }

    /**
     * 添加选项（企业端）
     */
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_DYNAMIC_OPTIONS_ENTERPRISE.value())")
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PostMapping
    public ResponseEntity<I18nResult<Boolean>> insert(@RequestBody DynamicOptionsEditParam editParam) {
        return dynamicOptionsService.insert(editParam).toResponseEntity();
    }

    /**
     * 修改选项（企业端）
     */
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_DYNAMIC_OPTIONS_ENTERPRISE.value())")
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PutMapping
    public ResponseEntity<I18nResult<Boolean>> put(@RequestBody DynamicOptionsEditParam editParam) {
        return dynamicOptionsService.put(editParam).toResponseEntity();
    }

    /**
     * 删除选项（企业端）
     */
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_DYNAMIC_OPTIONS_ENTERPRISE.value())")
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @DeleteMapping("/{id}")
    public ResponseEntity<I18nResult<Boolean>> deleteById(@PathVariable("id") String id) {
        return dynamicOptionsService.deleteById(id).toResponseEntity();
    }

    /**
     * 查询所有选项（企业端）
     */
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_DYNAMIC_OPTIONS_ENTERPRISE.value())")
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/query_all")
    public ResponseEntity<I18nResult<List<DynamicOptionsModel>>> queryAll(@Querying DynamicOptionsQueryParam queryParam) {
        return dynamicOptionsService.queryAll(queryParam).toResponseEntity();
    }

    /**
     * 查询所有选项（客户端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/client/query_all")
    public ResponseEntity<I18nResult<List<DynamicOptionsModel>>> clientQueryAll(@Querying DynamicOptionsQueryParam queryParam) {
        return dynamicOptionsService.clientQueryAll(queryParam).toResponseEntity();
    }

    /**
     * 查询所有选项通过Type（企业端）
     */
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_DYNAMIC_OPTIONS_ENTERPRISE.value())")
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/query_all_by_type")
    public ResponseEntity<I18nResult<List<DynamicOptionsModel>>> queryAllByType(@Querying DynamicOptionsQueryParam queryParam) {
        return dynamicOptionsService.queryAllByType(queryParam).toResponseEntity();
    }

    /**
     * 查询所有选项通过Type（客户端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/client/query_all_by_type")
    public ResponseEntity<I18nResult<List<DynamicOptionsModel>>> clientQueryAllByType(@Querying DynamicOptionsQueryParam queryParam) {
        return dynamicOptionsService.clientQueryAllByType(queryParam).toResponseEntity();
    }


    /**
     * 根据id查询选项（企业端）
     */
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_DYNAMIC_OPTIONS_ENTERPRISE.value())")
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/{id}")
    public ResponseEntity<I18nResult<DynamicOptionsModel>> queryOneById(@PathVariable("id") String id) {
        return dynamicOptionsService.queryOneById(id).toResponseEntity();
    }


}
