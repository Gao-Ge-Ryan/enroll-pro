package top.gaogle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.gaogle.framework.annotation.Querying;
import top.gaogle.framework.annotation.RateLimiter;
import top.gaogle.framework.i18n.I18nResult;
import top.gaogle.framework.pojo.PageModel;
import top.gaogle.pojo.domain.OperateLog;
import top.gaogle.pojo.enums.LimitType;
import top.gaogle.pojo.param.OperateLogQueryParam;
import top.gaogle.service.OperateLogService;

/**
 * 操作日志
 *
 * @author gaogle
 * @since 1.0.0
 */
@RestController
@RequestMapping("/operate_log")
public class OperateLogController {
    private final OperateLogService operateLogService;

    @Autowired
    public OperateLogController(OperateLogService operateLogService) {
        this.operateLogService = operateLogService;
    }

    /**
     * 条件分页查询
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).OPERATION_LOG_VIEW_ADMIN.value())")
    @GetMapping("/page")
    public ResponseEntity<I18nResult<PageModel<OperateLog>>> queryByPageAndCondition(@Querying OperateLogQueryParam queryParam) {
        return operateLogService.queryByPageAndCondition(queryParam).toResponseEntity();
    }
}
