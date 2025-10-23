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
import top.gaogle.pojo.model.RegisterPublishAnnouncementModel;
import top.gaogle.pojo.param.RegisterPublishAnnouncementEditParam;
import top.gaogle.pojo.param.RegisterPublishAnnouncementQueryParam;
import top.gaogle.service.RegisterPublishAnnouncementService;

/**
 * 报名发布公告
 *
 * @author gaogle
 * @since 1.0.0
 */
@RestController
@RequestMapping("/register_publish_announcement")
public class RegisterPublishAnnouncementController {
    private final RegisterPublishAnnouncementService registerPublishAnnouncementService;

    @Autowired
    public RegisterPublishAnnouncementController(RegisterPublishAnnouncementService registerPublishAnnouncementService) {
        this.registerPublishAnnouncementService = registerPublishAnnouncementService;
    }

//    /**
//     * 新闻动态新增(企业端)
//     */
//    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
//    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_ANNOUNCEMENT_ENTERPRISE.value())")
//    @PostMapping("/news")
//    public ResponseEntity<I18nResult<Boolean>> newsAdd(@RequestBody RegisterPublishAnnouncementEditParam editParam) {
//        return registerPublishAnnouncementService.newsAdd(editParam).toResponseEntity();
//    }

    /**
     * 报名发布新增(企业端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_ANNOUNCEMENT_ENTERPRISE.value())")
    @PostMapping
    public ResponseEntity<I18nResult<Boolean>> add(@RequestBody RegisterPublishAnnouncementEditParam editParam) {
        return registerPublishAnnouncementService.add(editParam).toResponseEntity();
    }

    /**
     * 分页条件查询(用户端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/page")
    public ResponseEntity<I18nResult<PageModel<RegisterPublishAnnouncementModel>>> queryByPageAndCondition(@Querying RegisterPublishAnnouncementQueryParam queryParam) {
        return registerPublishAnnouncementService.queryByPageAndCondition(queryParam).toResponseEntity();
    }

    /**
     * 修改（企业端）
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_ANNOUNCEMENT_ENTERPRISE.value())")
    @PutMapping
    public ResponseEntity<I18nResult<Boolean>> put(@RequestBody RegisterPublishAnnouncementEditParam editParam) {
        return registerPublishAnnouncementService.put(editParam).toResponseEntity();
    }

    /**
     * 根据id查询详情(用户端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/{id}")
    public ResponseEntity<I18nResult<RegisterPublishAnnouncementModel>> queryOneById(@PathVariable("id") String id) {
        return registerPublishAnnouncementService.queryOneById(id).toResponseEntity();
    }

    /**
     * 根据id删除(企业端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_ANNOUNCEMENT_ENTERPRISE.value())")
    @DeleteMapping("/{id}")
    public ResponseEntity<I18nResult<Boolean>> deleteById(@PathVariable("id") String id) {
        return registerPublishAnnouncementService.deleteById(id).toResponseEntity();
    }

}
