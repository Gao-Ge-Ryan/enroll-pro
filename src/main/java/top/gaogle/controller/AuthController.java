package top.gaogle.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.gaogle.framework.annotation.Anonymous;
import top.gaogle.framework.annotation.RateLimiter;
import top.gaogle.framework.i18n.I18nResult;
import top.gaogle.pojo.domain.AuthenticationPacket;
import top.gaogle.pojo.dto.RegistryDTO;
import top.gaogle.pojo.dto.VerificationCodeDTO;
import top.gaogle.pojo.enums.LimitType;
import top.gaogle.service.AuthService;

/**
 * 认证
 *
 * @author gaogle
 * @since 1.0.0
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;


    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 获取邮箱验证码
     */
    @Anonymous
    @RateLimiter(time = 120, count = 5, limitType = LimitType.IP)
    @PostMapping("/verification_code")
    public ResponseEntity<I18nResult<Boolean>> verificationCode(@RequestBody VerificationCodeDTO verificationCodeDTO) {
        return authService.verificationCode(verificationCodeDTO).toResponseEntity();
    }

    /**
     * 注册接口
     */
    @Anonymous
    @RateLimiter(time = 120, count = 5, limitType = LimitType.IP)
    @PostMapping("/registry")
    public ResponseEntity<I18nResult<Object>> registry(@RequestBody RegistryDTO registryDTO) {
        return authService.registry(registryDTO).toResponseEntity();
    }

    /**
     * 重置密码（用户端）
     */
    @Anonymous
    @RateLimiter(time = 120, count = 5, limitType = LimitType.IP)
    @PutMapping("/reset_password")
    public ResponseEntity<I18nResult<Boolean>> resetPassword(@RequestBody RegistryDTO registryDTO) {
        return authService.resetPassword(registryDTO).toResponseEntity();
    }

    /**
     * 邮箱登录接口
     */
    @Anonymous
    @RateLimiter(time = 120, count = 5, limitType = LimitType.IP)
    @PostMapping("/email_login")
    public ResponseEntity<I18nResult<String>> emailLogin(@RequestBody AuthenticationPacket authenticationPacket) {
        return authService.emailLogin(authenticationPacket).toResponseEntity();
    }

    /**
     * 登录接口
     */
    @Anonymous
    @RateLimiter(time = 120, count = 5, limitType = LimitType.IP)
    @PostMapping("/login")
    public ResponseEntity<I18nResult<String>> login(@RequestBody AuthenticationPacket authenticationPacket) {
        return authService.login(authenticationPacket).toResponseEntity();
    }

    /**
     * 管理员模拟登录接口
     */
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).USER_ADMIN_SIMULATION_LOGIN_ADMIN.value())")
    @RateLimiter(time = 120, count = 5, limitType = LimitType.IP)
    @PostMapping("/admin/simulation/login")
    public ResponseEntity<I18nResult<String>> adminSimulationLogin(@RequestBody AuthenticationPacket authenticationPacket) {
        return authService.adminSimulationLogin(authenticationPacket).toResponseEntity();
    }
}

