package top.gaogle.service;


import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.util.CastUtils;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import top.gaogle.common.RegisterConst;
import top.gaogle.dao.master.UserMapper;
import top.gaogle.framework.config.GaogleConfig;
import top.gaogle.framework.i18n.I18ResultCode;
import top.gaogle.framework.i18n.I18nResult;
import top.gaogle.framework.security.Auth0TokenUtil;
import top.gaogle.framework.security.UserDetailsCustomizer;
import top.gaogle.framework.util.*;
import top.gaogle.pojo.domain.AuthenticationPacket;
import top.gaogle.pojo.dto.RegistryDTO;
import top.gaogle.pojo.dto.VerificationCodeDTO;
import top.gaogle.pojo.entity.UserEntity;
import top.gaogle.pojo.enums.HttpStatusEnum;
import top.gaogle.pojo.enums.VerificationCodeTypeEnum;
import top.gaogle.pojo.model.UserModel;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService extends SuperService {

    @Value("${spring.mail.username}")
    public String platformMail;

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final StringRedisTemplate stringRedisTemplate;
    private final AuthenticationManager authenticationManager;
    private final CacheUtil cacheUtil;
    private final UserDetailsService userDetailsService;


    @Autowired
    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, EmailService emailService, TemplateEngine templateEngine, StringRedisTemplate stringRedisTemplate, AuthenticationManager authenticationManager, CacheUtil cacheUtil, UserDetailsService userDetailsService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.templateEngine = templateEngine;
        this.stringRedisTemplate = stringRedisTemplate;
        this.authenticationManager = authenticationManager;
        this.cacheUtil = cacheUtil;
        this.userDetailsService = userDetailsService;
    }

    public I18nResult<Object> registry(RegistryDTO registryDTO) {
        I18nResult<Object> result = I18nResult.newInstance();
        try {
            String username = registryDTO.getUsername();
            String password = registryDTO.getPassword();
            String nickname = registryDTO.getNickname();
            String verificationCodeParam = registryDTO.getVerificationCode();
            if (StringUtils.isAnyEmpty(username, password, nickname, verificationCodeParam)) {
                return result.failed().setMessage(I18ResultCode.MESSAGE, "请输入正确的请求参数");
            }
            username = username.trim();
            if (Boolean.FALSE.equals(ValidatorUtil.regex(RegisterConst.EMAIL_REGEX, username))) {
                return result.failed().setMessage(I18ResultCode.MESSAGE, "请输入正确格式的邮箱账号");
            }
            if (Boolean.FALSE.equals(ValidatorUtil.regex(RegisterConst.PASSWORD_REGEX, password))) {
                return result.failed().setMessage(I18ResultCode.MESSAGE, "密码必须包含至少一个字母和一个数字,长度必须在8到20个字符之间,可以包含以下特殊字符（但不是必须的）：@$!%*#?&");
            }
            String redisKey = StringUtil.redisKey(RegisterConst.VERIFICATION_CODE, VerificationCodeTypeEnum.REGISTER.name(), username);
            String verificationCode = stringRedisTemplate.opsForValue().get(redisKey);
            if (StringUtils.isEmpty(verificationCode)) {
                return result.failed().setMessage(I18ResultCode.MESSAGE, "验证码失效，请重新获取验证码！");
            }
            if (!Objects.equals(verificationCodeParam, verificationCode)) {
                return result.failed().setMessage(I18ResultCode.MESSAGE, "验证码错误！");
            }

            if (!Boolean.TRUE.equals(Auth0TokenUtil.validateSignTime(GaogleConfig.getProjectSignTime()))) {
                return result.failedBadRequest().setMessage("系统错误-00:00:" + DateUtil.currentTimeMillis());
            }


            int userSize = userMapper.selectExistByUsername(username);
            if (userSize > 0) {
                return result.failed().setMessage(I18ResultCode.MESSAGE, username + "用户已注册");
            }
            UserEntity userEntity = new UserEntity();
            userEntity.setId(UniqueUtil.getUniqueId());
            userEntity.setPassword(passwordEncoder.encode(password));
            userEntity.setNickname(nickname);
            userEntity.setUsername(username);
            userEntity.setCreateBy(username);
            userEntity.setUpdateBy(username);
            Long timeMillis = DateUtil.currentTimeMillis();
            userEntity.setCreateAt(timeMillis);
            userEntity.setUpdateAt(timeMillis);
            userEntity.setDelFlag(false);
            userEntity.setDisabled(false);
            userMapper.insertOne(userEntity);
            stringRedisTemplate.delete(redisKey);
        } catch (Exception e) {
            log.error("注册失败：", e);
            return result.failed().setMessage(I18ResultCode.MESSAGE, "注册账号失败");
        }
        return result;
    }

    public I18nResult<Boolean> verificationCode(VerificationCodeDTO verificationCodeDTO) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String email = verificationCodeDTO.getEmail();
            VerificationCodeTypeEnum codeTypeEnum = verificationCodeDTO.getCodeTypeEnum();
            if (StringUtils.isAnyEmpty(email) || codeTypeEnum == null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage(I18ResultCode.MESSAGE, "缺少必要参数");
            }
            email = email.trim();
            if (Boolean.FALSE.equals(ValidatorUtil.regex(RegisterConst.EMAIL_REGEX, email))) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage(I18ResultCode.MESSAGE, "请输入正确格式的邮箱账号");
            }
            String title = codeTypeEnum.title();
            String captcha = CaptchaGeneratorUtil.generateCaptcha();
            stringRedisTemplate.opsForValue().set(StringUtil.redisKey(RegisterConst.VERIFICATION_CODE, codeTypeEnum.name(), email), captcha, 5, TimeUnit.MINUTES);
            String noticeTemplate = "<p style='font-family: Arial, sans-serif; font-size: 16px; color: #333;'>"
                    + "【<a href='#{clientUrl}' target='_blank' style='text-decoration: none; font-weight: bold; color: #3399FF;'>#{systemName}</a>】您的验证码为：<span style='font-weight: bold; color: #FFA500; font-size: 17px;'>#{captcha}</span>"
                    + "，该验证码5分钟内有效，请勿泄露于他人！</p>";
            String noticeHtml = noticeTemplate.replace("#{systemName}", GaogleConfig.getSystemName()).replace("#{captcha}", captcha).replace("#{clientUrl}", GaogleConfig.getClientUrl());
            Context context = new Context();
            context.setVariable("title", title);
            context.setVariable("noticeHtml", noticeHtml);
            String content = templateEngine.process("verificationCodeTemplate.html", context);
            emailService.send(GaogleConfig.getSystemName(), platformMail, email, title, content, true, null, null, null);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("获取验证码发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "获取验证码发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> resetPassword(RegistryDTO registryDTO) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String username = registryDTO.getUsername();
            String password = registryDTO.getPassword();
            String verificationCodeParam = registryDTO.getVerificationCode();
            if (StringUtils.isAnyEmpty(username, password, verificationCodeParam)) {
                return result.failed().setMessage(I18ResultCode.MESSAGE, "请输入正确的请求参数");
            }
            username = username.trim();
            if (Boolean.FALSE.equals(ValidatorUtil.regex(RegisterConst.EMAIL_REGEX, username))) {
                return result.failed().setMessage(I18ResultCode.MESSAGE, "请输入正确格式的邮箱账号");
            }
            if (Boolean.FALSE.equals(ValidatorUtil.regex(RegisterConst.PASSWORD_REGEX, password))) {
                return result.failed().setMessage(I18ResultCode.MESSAGE, "密码必须包含至少一个字母和一个数字,长度必须在8到20个字符之间,可以包含以下特殊字符（但不是必须的）：@$!%*#?&");
            }
            String redisKey = StringUtil.redisKey(RegisterConst.VERIFICATION_CODE, VerificationCodeTypeEnum.RESET_PASSWORDS.name(), username);
            String verificationCode = stringRedisTemplate.opsForValue().get(redisKey);
            if (StringUtils.isEmpty(verificationCode)) {
                return result.failed().setMessage(I18ResultCode.MESSAGE, "验证码失效，请重新获取验证码！");
            }
            if (!Objects.equals(verificationCodeParam, verificationCode)) {
                return result.failed().setMessage(I18ResultCode.MESSAGE, "验证码错误！");
            }

            int userSize = userMapper.selectExistByUsername(username);
            if (userSize < 1) {
                return result.failed().setMessage(I18ResultCode.MESSAGE, username + " 用户不存在");
            }
            userMapper.updatePasswordByUsername(username, passwordEncoder.encode(password));
            stringRedisTemplate.delete(redisKey);
        } catch (Exception e) {
            log.error("注册失败：", e);
            return result.failed().setMessage(I18ResultCode.MESSAGE, "注册账号失败");
        }
        return result;
    }

    public I18nResult<String> emailLogin(AuthenticationPacket authenticationPacket) {
        I18nResult<String> result = I18nResult.newInstance();
        try {
            String email = authenticationPacket.getEmail();
            String verificationCodeParam = authenticationPacket.getVerificationCode();
            if (StringUtils.isAnyEmpty(email, verificationCodeParam)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            email = email.trim();
            String redisKey = StringUtil.redisKey(RegisterConst.VERIFICATION_CODE, VerificationCodeTypeEnum.EMAIL_LOGIN.name(), email);
            String verificationCode = stringRedisTemplate.opsForValue().get(redisKey);
            if (StringUtils.isEmpty(verificationCode)) {
                return result.failedBadRequest().setMessage("验证码失效，请重新获取验证码");
            }
            if (!Objects.equals(verificationCodeParam, verificationCode)) {
                return result.failedBadRequest().setMessage("验证码错误");
            }
            UserModel userModel = userMapper.selectByUsername(email);
            if (userModel == null) {
                return result.failedBadRequest().setMessage("用户不存在，请先注册");
            }
            if (!Boolean.TRUE.equals(Auth0TokenUtil.validateSignTime(GaogleConfig.getProjectSignTime()))) {
                return result.failedBadRequest().setMessage("系统错误-00:00:" + DateUtil.currentTimeMillis());
            }
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            // 将用户信息存入 authentication，方便后续校验
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsCustomizer userDetailsCustomizer = CastUtils.cast(authentication.getPrincipal());
            String token = Auth0TokenUtil.generateToken(userDetailsCustomizer);
            cacheUtil.tokenCache(token);
            stringRedisTemplate.delete(redisKey);
            return result.succeed().setData(token);
        } catch (BadCredentialsException badCredentialsException) {
            log.error("账号或密码错误：", badCredentialsException);
            result.failedBadRequest().setMessage("账号或密码错误");
        } catch (AccountExpiredException accountExpiredException) {
            log.error("User account has expired：", accountExpiredException);
            result.failedBadRequest().setMessage("User account has expired");
        } catch (LockedException lockedException) {
            log.error("User account is locked：", lockedException);
            result.failedBadRequest().setMessage("User account is locked");
        } catch (DisabledException disabledException) {
            log.error("User is disabled：", disabledException);
            result.failedBadRequest().setMessage("User is disabled");
        } catch (Exception e) {
            log.error("登录失败：", e);
            result.failedBadRequest().setMessage("登录失败，请联系管理员");
        }
        return result;
    }

    public I18nResult<String> login(AuthenticationPacket authenticationPacket) {
        I18nResult<String> result = I18nResult.newInstance();
        try {
            String email = authenticationPacket.getEmail();
            String password = authenticationPacket.getPassword();
            if (StringUtils.isAnyEmpty(email, password)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            email = email.trim();
            UserModel userModel = userMapper.selectByUsername(email);
            if (userModel == null) {
                return result.failedBadRequest().setMessage("用户不存在，请先注册");
            }
            if (!Boolean.TRUE.equals(Auth0TokenUtil.validateSignTime(GaogleConfig.getProjectSignTime()))) {
                return result.failedBadRequest().setMessage("系统错误-00:00:" + DateUtil.currentTimeMillis());
            }
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsCustomizer userDetailsCustomizer = CastUtils.cast(authentication.getPrincipal());
            String token = Auth0TokenUtil.generateToken(userDetailsCustomizer);
            cacheUtil.tokenCache(token);
            return result.succeed().setData(token);
        } catch (BadCredentialsException badCredentialsException) {
            log.info("账号或密码错误：", badCredentialsException);
            result.failedBadRequest().setMessage("账号或密码错误");
        } catch (AccountExpiredException accountExpiredException) {
            log.error("User account has expired：", accountExpiredException);
            result.failedBadRequest().setMessage("User account has expired");
        } catch (LockedException lockedException) {
            log.error("User account is locked：", lockedException);
            result.failedBadRequest().setMessage("User account is locked");
        } catch (DisabledException disabledException) {
            log.error("User is disabled：", disabledException);
            result.failedBadRequest().setMessage("User is disabled");
        } catch (Exception e) {
            log.error("登录失败：", e);
            result.failedBadRequest().setMessage("登录失败，请联系管理员！");
        }
        return result;
    }

    public I18nResult<String> adminSimulationLogin(AuthenticationPacket authenticationPacket) {
        I18nResult<String> result = I18nResult.newInstance();
        try {
            String email = authenticationPacket.getEmail();
            if (StringUtils.isEmpty(email)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            email = email.trim();
            UserModel userModel = userMapper.selectByUsername(email);
            if (userModel == null) {
                return result.failedBadRequest().setMessage("用户不存在，请先注册");
            }
            if (!Boolean.TRUE.equals(Auth0TokenUtil.validateSignTime(GaogleConfig.getProjectSignTime()))) {
                return result.failedBadRequest().setMessage("系统错误-00:00:" + DateUtil.currentTimeMillis());
            }
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            // 将用户信息存入 authentication，方便后续校验
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsCustomizer userDetailsCustomizer = CastUtils.cast(authentication.getPrincipal());
            String token = Auth0TokenUtil.generateToken(userDetailsCustomizer);
            cacheUtil.tokenCache(token);
            return result.succeed().setData(token);
        } catch (BadCredentialsException badCredentialsException) {
            log.error("账号或密码错误：", badCredentialsException);
            result.failedBadRequest().setMessage("账号或密码错误");
        } catch (AccountExpiredException accountExpiredException) {
            log.error("User account has expired：", accountExpiredException);
            result.failedBadRequest().setMessage("User account has expired");
        } catch (LockedException lockedException) {
            log.error("User account is locked：", lockedException);
            result.failedBadRequest().setMessage("User account is locked");
        } catch (DisabledException disabledException) {
            log.error("User is disabled：", disabledException);
            result.failedBadRequest().setMessage("User is disabled");
        } catch (Exception e) {
            log.error("登录失败：", e);
            result.failedBadRequest().setMessage("登录失败，请联系管理员");
        }
        return result;

    }
}
