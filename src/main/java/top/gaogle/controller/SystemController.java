package top.gaogle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.gaogle.framework.annotation.Anonymous;
import top.gaogle.framework.annotation.RateLimiter;
import top.gaogle.framework.i18n.I18nResult;
import top.gaogle.pojo.dto.SystemAttributeDTO;
import top.gaogle.pojo.enums.LimitType;
import top.gaogle.service.SystemService;

/**
 * 系统管理
 *
 * @author gaogle
 * @since 1.0.0
 */
@RestController
@RequestMapping("/system")
public class SystemController {

    private final SystemService systemService;

    @Autowired
    public SystemController(SystemService systemService) {
        this.systemService = systemService;
    }

    /**
     * 系统属性配置
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @Anonymous
    @GetMapping("/attribute")
    public ResponseEntity<I18nResult<SystemAttributeDTO>> attribute() {
        return systemService.attribute().toResponseEntity();
    }

}
