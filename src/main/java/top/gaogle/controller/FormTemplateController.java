package top.gaogle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.gaogle.framework.annotation.RateLimiter;
import top.gaogle.framework.i18n.I18nResult;
import top.gaogle.pojo.enums.FormTemplateFlagEnum;
import top.gaogle.pojo.enums.LimitType;
import top.gaogle.pojo.model.FormTemplateModel;
import top.gaogle.pojo.model.InterviewTicketTemplateModel;
import top.gaogle.pojo.model.TicketTemplateModel;
import top.gaogle.service.FormTemplateService;

import java.util.List;

/**
 * 表单模板
 *
 * @author gaogle
 * @since 1.0.0
 */
@RestController
@RequestMapping("/form_template")
public class FormTemplateController {

    private final FormTemplateService formTemplateService;

    @Autowired
    public FormTemplateController(FormTemplateService formTemplateService) {
        this.formTemplateService = formTemplateService;
    }

//    /**
//     * 新增表单
//     */
//    @PostMapping
//    public ResponseEntity<I18nResult<Boolean>> add(@RequestBody FormTemplateEditParam editParam) {
//        return formTemplateService.add(editParam).toResponseEntity();
//    }
//
//    /**
//     * 修改表单
//     */
//    @PutMapping
//    public ResponseEntity<I18nResult<Boolean>> put(@RequestBody FormTemplateEditParam editParam) {
//        return formTemplateService.put(editParam).toResponseEntity();
//    }
//
//    /**
//     * 分页条件查询
//     */
//    @GetMapping("/page")
//    public ResponseEntity<I18nResult<PageModel<FormTemplateModel>>> queryByPageAndCondition(@Querying FormTemplateQueryParam queryParam) {
//        return formTemplateService.queryByPageAndCondition(queryParam).toResponseEntity();
//    }
//
//    /**
//     * 根据id查询详情
//     */
//    @GetMapping("/{id}")
//    public ResponseEntity<I18nResult<FormTemplateModel>> queryOneById(@PathVariable("id") String id) {
//        return formTemplateService.queryOneById(id).toResponseEntity();
//    }
//
//    /**
//     * 根据id删除表单
//     */
//    @DeleteMapping("/{id}")
//    public ResponseEntity<I18nResult<Boolean>> deleteById(@PathVariable("id") String id) {
//        return formTemplateService.deleteById(id).toResponseEntity();
//    }

    /**
     * 查询所有表单模板（企业端）
     */
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_FORM_TEMPLATE_ENTERPRISE.value())")
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/enterprise/query/all")
    public ResponseEntity<I18nResult<List<FormTemplateModel>>> queryAll() {
        return formTemplateService.queryAll().toResponseEntity();
    }

    /**
     * 查询表单模板下对应的笔试证件模板（企业端）
     */
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_FORM_TEMPLATE_ENTERPRISE.value())")
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/enterprise/query_all_ticket")
    public ResponseEntity<I18nResult<List<TicketTemplateModel>>> queryAllByFormTemplateId(@RequestParam FormTemplateFlagEnum flag) {
        return formTemplateService.queryAllByFormTemplateId(flag).toResponseEntity();
    }

    /**
     * 查询表单模板下对应的面试证件模板（企业端）
     */
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_FORM_TEMPLATE_ENTERPRISE.value())")
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/enterprise/query_all_interview_ticket")
    public ResponseEntity<I18nResult<List<InterviewTicketTemplateModel>>> queryAllInterviewByFormTemplateFlag(@RequestParam FormTemplateFlagEnum flag) {
        return formTemplateService.queryAllInterviewByFormTemplateFlag(flag).toResponseEntity();
    }


}
