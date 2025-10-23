package top.gaogle.service;

import com.github.pagehelper.page.PageMethod;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import top.gaogle.common.RegisterConst;
import top.gaogle.dao.master.*;
import top.gaogle.framework.config.GaogleConfig;
import top.gaogle.framework.i18n.I18ResultCode;
import top.gaogle.framework.i18n.I18nResult;
import top.gaogle.framework.pojo.CertificateDTO;
import top.gaogle.framework.pojo.PageModel;
import top.gaogle.framework.util.*;
import top.gaogle.pojo.dto.ApproveDTO;
import top.gaogle.pojo.dto.PutUserRoleDTO;
import top.gaogle.pojo.dto.RegisterPublishStatusCountDTO;
import top.gaogle.pojo.dto.TestDataDTO;
import top.gaogle.pojo.enums.*;
import top.gaogle.pojo.model.*;
import top.gaogle.pojo.param.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static top.gaogle.common.RegisterConst.GENERAL_HTML;

@Service
public class EnterpriseService extends SuperService {
    private final EnterpriseMapper enterpriseMapper;
    private final RoleMapper roleMapper;
    private final UserMapper userMapper;
    private final TransactionTemplate transactionTemplate;
    private final UserRoleMapper userRoleMapper;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final EnterpriseServeMapper enterpriseServeMapper;
    private final EnterprisePartnerMapper enterprisePartnerMapper;
    private final EnterpriseSlideshowMapper enterpriseSlideshowMapper;
    private final EnterpriseNewsMapper enterpriseNewsMapper;
    private final EnterpriseBillMapper enterpriseBillMapper;
    private final SpotInfoMapper spotInfoMapper;
    private final RegisterPublishMapper registerPublishMapper;

    @Autowired
    public EnterpriseService(EnterpriseMapper enterpriseMapper, RoleMapper roleMapper, UserMapper userMapper, @Qualifier("transactionTemplate") TransactionTemplate transactionTemplate, UserRoleMapper userRoleMapper, EmailService emailService, TemplateEngine templateEngine, EnterpriseServeMapper enterpriseServeMapper, EnterprisePartnerMapper enterprisePartnerMapper, EnterpriseSlideshowMapper enterpriseSlideshowMapper, EnterpriseNewsMapper enterpriseNewsMapper, EnterpriseBillMapper enterpriseBillMapper, SpotInfoMapper spotInfoMapper, RegisterPublishMapper registerPublishMapper) {
        this.enterpriseMapper = enterpriseMapper;
        this.roleMapper = roleMapper;
        this.userMapper = userMapper;
        this.transactionTemplate = transactionTemplate;
        this.userRoleMapper = userRoleMapper;
        this.emailService = emailService;
        this.templateEngine = templateEngine;
        this.enterpriseServeMapper = enterpriseServeMapper;
        this.enterprisePartnerMapper = enterprisePartnerMapper;
        this.enterpriseSlideshowMapper = enterpriseSlideshowMapper;
        this.enterpriseNewsMapper = enterpriseNewsMapper;
        this.enterpriseBillMapper = enterpriseBillMapper;
        this.spotInfoMapper = spotInfoMapper;
        this.registerPublishMapper = registerPublishMapper;
    }


    public I18nResult<Boolean> insert(EnterpriseEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            if (StringUtils.isAnyEmpty()) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage(I18ResultCode.MESSAGE, "缺少必要参数");
            }
            editParam.setId(UniqueUtil.getUniqueId());
            Long timeMillis = DateUtil.currentTimeMillis();
            editParam.setCreateAt(timeMillis);
            editParam.setUpdateAt(timeMillis);
            String loginUsername = SecurityUtil.getLoginUsername();
            editParam.setCreateBy(loginUsername);
            editParam.setUpdateBy(loginUsername);
            enterpriseMapper.insert(editParam);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("添加企业发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "添加企业发生异常");
        }
        return result;
    }

    public I18nResult<PageModel<EnterpriseModel>> queryByPageAndCondition(EnterpriseQueryParam queryParam) {
        I18nResult<PageModel<EnterpriseModel>> result = I18nResult.newInstance();
        try {
            PageMethod.startPage(queryParam.getPageNum(), queryParam.getPageSize());
            List<EnterpriseModel> enterpriseModels = enterpriseMapper.queryByPageAndCondition(queryParam);
            PageModel<EnterpriseModel> pageModel = new PageModel<>(enterpriseModels);
            result.succeed().setData(pageModel);
        } catch (Exception e) {
            log.error("添加企业发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "添加企业发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> putEnterprise(EnterpriseEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            enterpriseMapper.putEnterprise(editParam);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("添加企业发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "添加企业发生异常");
        }
        return result;
    }

    public I18nResult<EnterpriseModel> deleteById(String id) {
        I18nResult<EnterpriseModel> result = I18nResult.newInstance();
        try {

            EnterpriseModel enterpriseModel = enterpriseMapper.queryOneById(id);
            if (enterpriseModel == null) {
                return result.failedBadRequest().setMessage("未查询到企业信息");
            }
            Long timeMillis = DateUtil.currentTimeMillis();
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(@NonNull TransactionStatus status) {
                    enterpriseMapper.deleteById(id);
                    enterpriseNewsMapper.deleteByEnterpriseId(id);
                    enterprisePartnerMapper.deleteByEnterpriseId(id);
                    enterpriseServeMapper.deleteByEnterpriseId(id);
                    enterpriseSlideshowMapper.deleteByEnterpriseId(id);
                    enterpriseBillMapper.deleteByEnterpriseId(id);
                    spotInfoMapper.deleteByEnterpriseId(id);
                    //删除员工和企业角色
                    List<String> accountBys = enterpriseMapper.queryAccountByByEnterpriseId(id);
                    List<String> enterpriseRoleIds = roleMapper.queryRoleIdsByType(RoleTypeEnum.ENTERPRISE_ROLE);
                    if (!CollectionUtils.isEmpty(enterpriseRoleIds) && !CollectionUtils.isEmpty(accountBys)) {
                        userRoleMapper.deleteByAccountBysAndEnterpriseRoleId(accountBys, enterpriseRoleIds);
                    }
                    registerPublishMapper.updateDelFlagToTrueByEnterpriseId(id, timeMillis);
                }
            });
            result.succeed().setData(enterpriseModel);
        } catch (Exception e) {
            log.error("删除企业发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "删除企业发生异常");
        }
        return result;
    }

    public I18nResult<EnterpriseModel> queryOneById(String id) {
        I18nResult<EnterpriseModel> result = I18nResult.newInstance();
        try {
            EnterpriseModel enterpriseModel = enterpriseMapper.queryOneById(id);
            result.succeed().setData(enterpriseModel);
        } catch (Exception e) {
            log.error("删除企业发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "删除企业发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> apply(EnterpriseEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String description = editParam.getDescription();
            String name = editParam.getName();
            String applyPhone = editParam.getApplyPhone();
            if (StringUtils.isAnyEmpty(name, description, applyPhone)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage(I18ResultCode.MESSAGE, "缺少必要参数");
            }
            String loginUsername = SecurityUtil.getLoginUsername();
            EnterpriseModel enterpriseModel = enterpriseMapper.queryByCreateBy(loginUsername);
            if (enterpriseModel != null) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage(I18ResultCode.MESSAGE, "该账号下存在企业");
            }
            EnterpriseModel enterpriseExistModel = enterpriseMapper.queryByAccountBy(loginUsername);
            if (enterpriseExistModel != null) {
                return result.failedBadRequest().setMessage("用户已加入 " + enterpriseExistModel.getName());
            }
            editParam.setId(UniqueUtil.getUniqueId());
            Long timeMillis = DateUtil.currentTimeMillis();
            editParam.setCreateAt(timeMillis);
            editParam.setUpdateAt(timeMillis);
            editParam.setCreateBy(loginUsername);
            editParam.setUpdateBy(loginUsername);
            editParam.setStatus(EnterpriseStatusEnum.PENDING);
            editParam.setShowStatus(EnterpriseShowStatusEnum.DISABLE);
            editParam.setBalance(0L);
            enterpriseMapper.insert(editParam);
            String endTitle = name + " 申请成为平台企业用户，请及时在管理端审核信息";
            String endMessage = " 申请成为平台企业用户，请及时审核企业信息";
            String noticeTemplate = "<p style='font-family: Arial, sans-serif; font-size: 16px; color: #333;'>"
                    + "【<a href='#{clientUrl}' target='_blank' style='text-decoration: none; font-weight: bold; color: #3399FF;'>#{systemName}</a>】#{title}"
                    + "#{endMessage}，更多信息请访问官方网址！</p>"
                    + "<div style='margin-top: 15px; text-align: center;'>"
                    + "<a href='#{clientUrl}' target='_blank' "
                    + "style='display: inline-block; padding: 10px 20px; background-color: rgba(0, 123, 255, 0.1); color: #336699; font-weight: 500; "
                    + "border-radius: 5px; text-decoration: none; border: 1px solid rgba(0,123,255,0.3);'>访问官网</a></div>";
            String noticeHtml = noticeTemplate.replace("#{systemName}", GaogleConfig.getSystemName()).replace("#{title}", name)
                    .replace("#{endMessage}", endMessage).replace("#{clientUrl}", GaogleConfig.getClientUrl());
            Context context = new Context();
            context.setVariable("title", endTitle);
            context.setVariable("noticeHtml", noticeHtml);
            String content = templateEngine.process(GENERAL_HTML, context);
            emailService.sendHTML(GaogleConfig.getAdminEmail(), endTitle, content);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("添加企业发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "添加企业发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> approve(String enterpriseId) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            if (StringUtils.isAnyEmpty(enterpriseId)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            EnterpriseModel enterpriseModel = enterpriseMapper.queryOneById(enterpriseId);
            if (enterpriseModel == null) {
                return result.failedBadRequest().setMessage("企业信息不存在");
            }
            if (!EnterpriseStatusEnum.PENDING.equals(enterpriseModel.getStatus())) {
                return result.failedBadRequest().setMessage("不是待审核状态不能操作");
            }
            enterpriseMapper.updateStatusAndReasonById(enterpriseId, EnterpriseStatusEnum.APPROVED, "");
            String name = enterpriseModel.getName();
            String endTitle = name + " 企业审核通过";
            String endMessage = "企业信息已审核通过";
            String noticeTemplate = "<p style='font-family: Arial, sans-serif; font-size: 16px; color: #333;'>"
                    + "【<a href='#{clientUrl}' target='_blank' style='text-decoration: none; font-weight: bold; color: #3399FF;'>#{systemName}</a>】您的#{title}"
                    + "#{endMessage}，更多信息请访问官方网址！</p>"
                    + "<div style='margin-top: 15px; text-align: center;'>"
                    + "<a href='#{clientUrl}' target='_blank' "
                    + "style='display: inline-block; padding: 10px 20px; background-color: rgba(0, 123, 255, 0.1); color: #336699; font-weight: 500; "
                    + "border-radius: 5px; text-decoration: none; border: 1px solid rgba(0,123,255,0.3);'>访问官网</a></div>";
            String noticeHtml = noticeTemplate.replace("#{systemName}", GaogleConfig.getSystemName()).replace("#{title}", name)
                    .replace("#{endMessage}", endMessage).replace("#{clientUrl}", GaogleConfig.getClientUrl());
            Context context = new Context();
            context.setVariable("title", endTitle);
            context.setVariable("noticeHtml", noticeHtml);
            String content = templateEngine.process(GENERAL_HTML, context);
            emailService.sendHTML(enterpriseModel.getCreateBy(), endTitle, content);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("添加企业发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "添加企业发生异常");
        }
        return result;
    }

    public I18nResult<List<EnterpriseModel>> queryAllByAndCondition(EnterpriseQueryParam queryParam) {
        I18nResult<List<EnterpriseModel>> result = I18nResult.newInstance();
        try {
            EnterpriseEditParam editParam = new EnterpriseEditParam();
            editParam.setStatus(EnterpriseStatusEnum.APPROVED);
            List<EnterpriseModel> enterpriseModels = enterpriseMapper.queryAllByAndCondition(queryParam);
            result.succeed().setData(enterpriseModels);
        } catch (Exception e) {
            log.error("添加企业发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "添加企业发生异常");
        }
        return result;
    }

    public I18nResult<PageModel<EnterpriseModel>> clientQueryByPage(EnterpriseQueryParam queryParam) {
        I18nResult<PageModel<EnterpriseModel>> result = I18nResult.newInstance();
        try {
            PageMethod.startPage(queryParam.getPageNum(), queryParam.getPageSize());
            List<EnterpriseModel> enterpriseModels = enterpriseMapper.clientQueryByPage(queryParam);
            if (!CollectionUtils.isEmpty(enterpriseModels)) {
                Map<String, EnterpriseModel> enterpriseModelMap = enterpriseModels.stream()
                        .collect(Collectors.toMap(
                                EnterpriseModel::getId,
                                Function.identity(),
                                (oldValue, newValue) -> newValue // key 冲突时使用新值覆盖
                        ));

                Set<String> keySet = enterpriseModelMap.keySet();
                if (!CollectionUtils.isEmpty(keySet)) {
                    List<RegisterPublishStatusCountDTO> statusCountDTOS = registerPublishMapper.queryStatusCount(new ArrayList<>(keySet), RegisterPublishStatusEnum.REGISTRATION_ONGOING);
                    if (!CollectionUtils.isEmpty(statusCountDTOS)) {
                        Map<String, Integer> statusCountMap = statusCountDTOS.stream()
                                .collect(Collectors.toMap(
                                        RegisterPublishStatusCountDTO::getEnterpriseId,
                                        RegisterPublishStatusCountDTO::getCount,
                                        (oldValue, newValue) -> newValue  // key重复时使用新值覆盖旧值
                                ));
                        for (Map.Entry<String, Integer> entry : statusCountMap.entrySet()) {
                            String key = entry.getKey();
                            EnterpriseModel enterpriseModel = enterpriseModelMap.getOrDefault(key, null);
                            if (enterpriseModel != null) {
                                enterpriseModel.setOnGoingStatusCount(entry.getValue());
                            }
                        }

                    }
                }
            }
            PageModel<EnterpriseModel> pageModel = new PageModel<>(enterpriseModels);
            result.succeed().setData(pageModel);
        } catch (Exception e) {
            log.error("添加企业发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "添加企业发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> enterprisePutEnterprise(EnterpriseEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String id = editParam.getId();
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isEmpty(id) || !Objects.equals(enterpriseId, id)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("参数错误");
            }
            enterpriseMapper.enterprisePutEnterprise(editParam);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("修改发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "修改发生异常");
        }
        return result;
    }

    public I18nResult<EnterpriseModel> clientQueryEnterprise(String enterpriseId) {
        I18nResult<EnterpriseModel> result = I18nResult.newInstance();
        try {
            if (StringUtils.isEmpty(enterpriseId)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage("缺失必要参数");
            }
            EnterpriseModel enterpriseModel = enterpriseMapper.clientQueryEnterprise(enterpriseId);
            result.succeed().setData(enterpriseModel);
        } catch (Exception e) {
            log.error("客户端查询企业详情发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "客户端查询企业详情发生异常");
        }
        return result;
    }

    public I18nResult<EnterpriseModel> enterpriseQueryEnterprise() {
        I18nResult<EnterpriseModel> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(enterpriseId)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            EnterpriseModel enterpriseModel = enterpriseMapper.queryOneById(enterpriseId);
            result.succeed().setData(enterpriseModel);
        } catch (Exception e) {
            log.error("添加企业发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "添加企业发生异常");
        }
        return result;
    }

    public I18nResult<TestEnumModel> testData(TestDataDTO testDataDTO) {
        I18nResult<TestEnumModel> result = I18nResult.newInstance();
        TestEnumModel testEnumModel = new TestEnumModel();
        List<BusinessTypeEnum> businessTypes = new ArrayList<>();
        businessTypes.add(BusinessTypeEnum.CLEAN);
        businessTypes.add(BusinessTypeEnum.UPDATE);
        businessTypes.add(BusinessTypeEnum.DELETE);
        testEnumModel.setBusinessTypes(businessTypes);
        testEnumModel.setCommentStatus(CommentStatusEnum.NEW);
        testEnumModel.setName("gaogle12345678910111213");
        log.info("测试请求yu");
        log.error("收到来自服务器的请求:{}", JsonUtil.object2Json(testDataDTO));
        return result.succeed().setData(testEnumModel);

    }

    public I18nResult<String> enterpriseQueryEnterpriseBalance() {
        I18nResult<String> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(enterpriseId)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            long balance = enterpriseMapper.queryBalanceById(enterpriseId);
            result.succeed().setData(StringUtil.amountLong2String(balance));
        } catch (Exception e) {
            log.error("查询企业余额发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "查询企业余额发生异常");
        }
        return result;
    }

    public I18nResult<PageModel<EnterpriseUserModel>> queryEnterpriseUserByPageAndCondition(EnterpriseUserQueryParam queryParam) {
        I18nResult<PageModel<EnterpriseUserModel>> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(enterpriseId)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            PageMethod.startPage(queryParam.getPageNum(), queryParam.getPageSize());
            List<EnterpriseUserModel> enterpriseUserModels = enterpriseMapper.queryUserByEnterpriseId(enterpriseId);
            List<String> usernames = enterpriseUserModels.stream().map(EnterpriseUserModel::getAccountBy).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(usernames)) {
                List<RoleModel> roleModels = roleMapper.queryRolesByUsernamesAndType(usernames, RoleTypeEnum.ENTERPRISE_ROLE);
                Map<String, List<RoleModel>> usernameRoleGroup = roleModels.stream().collect(Collectors.groupingBy(RoleModel::getAccountBy));
                for (EnterpriseUserModel userModel : enterpriseUserModels) {
                    userModel.setRoleModels(usernameRoleGroup.getOrDefault(userModel.getAccountBy(), null));
                }
            }
            PageModel<EnterpriseUserModel> pageModel = new PageModel<>(enterpriseUserModels);
            result.succeed().setData(pageModel);
        } catch (Exception e) {
            log.error("查询发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "查询发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> addUser(EnterpriseUserEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            String accountBy = editParam.getAccountBy();
            if (StringUtils.isAnyEmpty(enterpriseId, accountBy)) {
                return result.failed().setStatus(HttpStatusEnum.BAD_REQUEST).setMessage(I18ResultCode.MESSAGE, "缺少必要参数");
            }
            UserModel userModel = userMapper.selectByUsername(accountBy);
            if (userModel == null) {
                return result.failedBadRequest().setMessage("用户未注册，请先注册该账户");
            }
            EnterpriseModel enterpriseModel = enterpriseMapper.queryOneById(enterpriseId);
            if (enterpriseModel == null) {
                return result.failedBadRequest().setMessage("企业信息不存在");
            }
            if (!EnterpriseStatusEnum.APPROVED.equals(enterpriseModel.getStatus())) {
                return result.failedBadRequest().setMessage("不是审核通过");
            }
            if (Objects.equals(accountBy, enterpriseModel.getCreateBy())) {
                return result.failedBadRequest().setMessage("该账户为企业拥有者无需加入");
            }
            EnterpriseModel enterpriseExistModel = enterpriseMapper.queryByAccountBy(accountBy);
            if (enterpriseExistModel != null) {
                return result.failedBadRequest().setMessage("用户已加入 " + enterpriseExistModel.getName());
            }
            editParam.setEnterpriseId(enterpriseId);
            editParam.setId(UniqueUtil.getUniqueId());
            Long timeMillis = DateUtil.currentTimeMillis();
            editParam.setCreateAt(timeMillis);
            editParam.setUpdateAt(timeMillis);
            String loginUsername = SecurityUtil.getLoginUsername();
            editParam.setCreateBy(loginUsername);
            editParam.setUpdateBy(loginUsername);
            enterpriseMapper.insertUser(editParam);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("添加发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "添加发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> putUserRole(PutUserRoleDTO putUserRoleDTO) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String accountBy = putUserRoleDTO.getAccountBy();
            List<String> roleIds = putUserRoleDTO.getRoleIds();
            String loginUsername = SecurityUtil.getLoginUsername();
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(accountBy, enterpriseId, loginUsername)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            EnterpriseUserModel enterpriseUserModel = enterpriseMapper.queryUserByAccountBy(accountBy);
            if (enterpriseUserModel == null) {
                return result.failedBadRequest().setMessage(accountBy + "未加入到企业");
            }

            if (!Objects.equals(enterpriseUserModel.getEnterpriseId(), enterpriseId)) {
                return result.failedBadRequest().setMessage("没有权限，禁止操作");
            }

            List<String> enterpriseRoleIds = roleMapper.queryRoleIdsByType(RoleTypeEnum.ENTERPRISE_ROLE);
            if (CollectionUtils.isEmpty(enterpriseRoleIds)) {
                return result.failedBadRequest().setMessage("企业角色为空禁止操作");
            }
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(@NonNull TransactionStatus status) {
                    userRoleMapper.deleteEnterpriseUserByAccountByAndEnterpriseRoleId(accountBy, enterpriseRoleIds);
                    Long timeMillis = DateUtil.currentTimeMillis();
                    for (String roleId : roleIds) {
                        if (enterpriseRoleIds.contains(roleId)) {
                            UserRoleEditParam editParam = new UserRoleEditParam();
                            editParam.setId(UniqueUtil.getUniqueId());
                            editParam.setAccountBy(accountBy);
                            editParam.setRoleId(roleId);
                            editParam.setCreateAt(timeMillis);
                            editParam.setUpdateAt(timeMillis);

                            editParam.setCreateBy(loginUsername);
                            editParam.setUpdateBy(loginUsername);
                            userRoleMapper.insert(editParam);
                        }
                    }
                }
            });
        } catch (Exception e) {
            log.error("修改发生异常：", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "修改发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> deleteUser(String accountBy) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(accountBy)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }

            EnterpriseUserModel enterpriseUserModel = enterpriseMapper.queryUserByAccountBy(accountBy);
            if (enterpriseUserModel == null) {
                return result.failedBadRequest().setMessage(accountBy + "未加入到企业");
            }

            if (!Objects.equals(enterpriseUserModel.getEnterpriseId(), enterpriseId)) {
                return result.failedBadRequest().setMessage("没有权限，禁止操作");
            }
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(@NonNull TransactionStatus status) {
                    List<String> enterpriseRoleIds = roleMapper.queryRoleIdsByType(RoleTypeEnum.ENTERPRISE_ROLE);
                    if (!CollectionUtils.isEmpty(enterpriseRoleIds)) {
                        userRoleMapper.deleteEnterpriseUserByAccountByAndEnterpriseRoleId(accountBy, enterpriseRoleIds);
                    }
                    enterpriseMapper.deleteUserByAccountBy(accountBy);
                }
            });
        } catch (Exception e) {
            log.error("修改发生异常：", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "修改发生异常");
        }
        return result;
    }

    public I18nResult<List<RoleModel>> enterpriseAllRole() {
        I18nResult<List<RoleModel>> result = I18nResult.newInstance();
        try {
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(enterpriseId)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            List<RoleModel> roleModels = roleMapper.queryByType(RoleTypeEnum.ENTERPRISE_ROLE);
            result.succeed().setData(roleModels);
        } catch (Exception e) {
            log.error("查询所发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "查询发生异常");
        }
        return result;
    }

    public void testDataWe(EnterprisePartnerQueryParam queryParam) {
        List<UserModel> userModels = userMapper.testOrderBy(queryParam);
        System.out.println(userModels);
    }

    public I18nResult<Boolean> enterpriseAlipaySecret(AlipaySecretEditParam editParam) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String alipayAppId = editParam.getAlipayAppId();
            String alipayAlipayPublicKey = editParam.getAlipayAlipayPublicKey();
            String alipayPrivateKey = editParam.getAlipayPrivateKey();
            String alipaySignType = editParam.getAlipaySignType();
            String enterpriseId = SecurityUtil.getEnterpriseId();
            if (StringUtils.isAnyEmpty(alipayAppId, alipayAlipayPublicKey, alipayPrivateKey, alipaySignType, enterpriseId)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            CertificateDTO certificate = CertificateUtil.loadCertificate(GaogleConfig.getKeystorePath() + File.separator + RegisterConst.ALIPAY_KEYSTORE,
                    GaogleConfig.getAlipayStorePass(), RegisterConst.ALIPAY_CERT);
            // 16 bytes随机Salt:
            byte[] salt = SecureRandom.getInstanceStrong().generateSeed(16);
            byte[] encryptedSalt = CertificateUtil.encrypt(certificate.getCertificate(), salt);
            byte[] encryptAlipayPrivateKey = CertificateUtil.encryptByPBE(GaogleConfig.getPassAES(), salt, alipayPrivateKey.getBytes(StandardCharsets.UTF_8));
            byte[] encryptAlipayAlipayPublicKey = CertificateUtil.encryptByPBE(GaogleConfig.getPassAES(), salt, alipayAlipayPublicKey.getBytes(StandardCharsets.UTF_8));

            editParam.setAlipaySalt(Base64.getEncoder().encodeToString(encryptedSalt));
            editParam.setAlipayAlipayPublicKey(Base64.getEncoder().encodeToString(encryptAlipayAlipayPublicKey));
            editParam.setAlipayPrivateKey(Base64.getEncoder().encodeToString(encryptAlipayPrivateKey));
            editParam.setEnterpriseId(enterpriseId);
            enterpriseMapper.putEnterpriseAlipaySecret(editParam);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("修改发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "修改发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> approveNot(String enterpriseId, ApproveDTO approveDTO) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            if (StringUtils.isAnyEmpty(enterpriseId)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            EnterpriseModel enterpriseModel = enterpriseMapper.queryOneById(enterpriseId);
            if (enterpriseModel == null) {
                return result.failedBadRequest().setMessage("企业信息不存在");
            }
            if (!EnterpriseStatusEnum.PENDING.equals(enterpriseModel.getStatus())) {
                return result.failedBadRequest().setMessage("不是待审核状态不能操作");
            }
            enterpriseMapper.deleteById(enterpriseModel.getId());
            String name = enterpriseModel.getName();
            String endTitle = name + " 企业审核未通过";
            String endMessage = "企业信息审核不通过，请重新申请";
            String noticeTemplate = "<p style='font-family: Arial, sans-serif; font-size: 16px; color: #333;'>"
                    + "【<a href='#{clientUrl}' target='_blank' style='text-decoration: none; font-weight: bold; color: #3399FF;'>#{systemName}</a>】您的#{title}"
                    + "#{endMessage}，更多信息请访问官方网址！</p>"
                    + "<div style='margin-top: 15px; text-align: center;'>"
                    + "<a href='#{clientUrl}' target='_blank' "
                    + "style='display: inline-block; padding: 10px 20px; background-color: rgba(0, 123, 255, 0.1); color: #336699; font-weight: 500; "
                    + "border-radius: 5px; text-decoration: none; border: 1px solid rgba(0,123,255,0.3);'>访问官网</a></div>";
            String noticeHtml = noticeTemplate.replace("#{systemName}", GaogleConfig.getSystemName()).replace("#{title}", name)
                    .replace("#{endMessage}", endMessage).replace("#{clientUrl}", GaogleConfig.getClientUrl());
            Context context = new Context();
            context.setVariable("title", endTitle);
            context.setVariable("noticeHtml", noticeHtml);
            String content = templateEngine.process(GENERAL_HTML, context);
            emailService.sendHTML(enterpriseModel.getCreateBy(), endTitle, content);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> approveRevocation() {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String loginUsername = SecurityUtil.getLoginUsername();
            EnterpriseModel enterpriseModel = enterpriseMapper.queryByCreateBy(loginUsername);
            if (enterpriseModel == null) {
                return result.failedBadRequest().setMessage("企业信息不存在");
            }
            if (!EnterpriseStatusEnum.PENDING.equals(enterpriseModel.getStatus())) {
                return result.failedBadRequest().setMessage("不是待审核状态不能操作");
            }
            enterpriseMapper.updateStatusAndReasonById(enterpriseModel.getId(), EnterpriseStatusEnum.EDITABLE, "");
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("添加企业发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "添加企业发生异常");
        }
        return result;
    }

    public I18nResult<ApproveDTO> clientQueryApprove() {
        I18nResult<ApproveDTO> result = I18nResult.newInstance();
        try {
            String loginUsername = SecurityUtil.getLoginUsername();
            EnterpriseModel enterpriseModel = enterpriseMapper.queryByAccountBy(loginUsername);
            ApproveDTO approveDTO = new ApproveDTO();
            if (enterpriseModel != null) {
                approveDTO.setName(enterpriseModel.getName());
                approveDTO.setApplyPhone(enterpriseModel.getApplyPhone());
                approveDTO.setDescription(enterpriseModel.getDescription());
                approveDTO.setLogo(enterpriseModel.getLogo());
                approveDTO.setReason(enterpriseModel.getReason());
                approveDTO.setStatus(enterpriseModel.getStatus());
            }
            result.succeed().setData(approveDTO);
        } catch (Exception e) {
            log.error("添加企业发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "添加企业发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> approveInfoUpdate(ApproveDTO approveDTO) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String loginUsername = SecurityUtil.getLoginUsername();
            String applyPhone = approveDTO.getApplyPhone();
            String name = approveDTO.getName();
            String description = approveDTO.getDescription();
            if (StringUtils.isAnyEmpty(loginUsername, applyPhone, name, description)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            EnterpriseModel enterpriseModel = enterpriseMapper.queryByCreateBy(loginUsername);
            if (enterpriseModel == null) {
                return result.failedBadRequest().setMessage("企业信息不存在");
            }
            EnterpriseStatusEnum status = enterpriseModel.getStatus();
            if (!EnterpriseStatusEnum.EDITABLE.equals(status) && !EnterpriseStatusEnum.REJECTED.equals(status)) {
                return result.failedBadRequest().setMessage("该状态不能修改");
            }
            approveDTO.setEnterpriseId(enterpriseModel.getId());
            approveDTO.setStatus(EnterpriseStatusEnum.PENDING);
            enterpriseMapper.approveInfoUpdate(approveDTO);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("添加企业发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "添加企业发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> clientDeleteApproveInfo() {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            String loginUsername = SecurityUtil.getLoginUsername();
            if (StringUtils.isAnyEmpty(loginUsername)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            EnterpriseModel enterpriseModel = enterpriseMapper.queryByCreateBy(loginUsername);
            if (enterpriseModel == null) {
                return result.failedBadRequest().setMessage("企业信息不存在");
            }
            EnterpriseStatusEnum status = enterpriseModel.getStatus();
            if (!EnterpriseStatusEnum.EDITABLE.equals(status) && !EnterpriseStatusEnum.REJECTED.equals(status)) {
                return result.failedBadRequest().setMessage("该状态禁止操作");
            }
            enterpriseMapper.deleteById(enterpriseModel.getId());
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "发生异常");
        }
        return result;
    }

    public I18nResult<Boolean> putShowStatus(String enterpriseId, EnterpriseShowStatusEnum showStatus) {
        I18nResult<Boolean> result = I18nResult.newInstance();
        try {
            if (StringUtils.isAnyEmpty(enterpriseId)) {
                return result.failedBadRequest().setMessage("缺失必要参数");
            }
            EnterpriseModel enterpriseModel = enterpriseMapper.queryOneById(enterpriseId);
            if (enterpriseModel == null) {
                return result.failedBadRequest().setMessage("企业信息不存在");
            }
            enterpriseMapper.updateShowStatusById(enterpriseId, showStatus);
            result.succeed().setData(true);
        } catch (Exception e) {
            log.error("添加企业发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "添加企业发生异常");
        }
        return result;
    }
}
