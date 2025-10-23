package top.gaogle.pojo.enums;

import top.gaogle.framework.pojo.AuthorityEnumModel;
import top.gaogle.framework.pojo.IndexedEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限枚举
 * <p>
 * shift取值范围：1~62，最高位符号位
 * MODULE所有权限二进制值：0111111111111111111111111111111111111111111111111111111111111110
 *
 * @author goge
 * @since 1.0.0
 */
public enum AuthorityEnum implements IndexedEnum<String> {

    /**
     * 用户管理
     */
    USER("user", "user", "用户管理", 0L, Type.MODULE),
    USER_VIEW_ADMIN(USER, "user_view_admin_admin", "管理端用户查看", 1L, Type.UNIT),
    USER_PUT_ADMIN(USER, "user_put_admin", "管理端用户修改", 2L, Type.UNIT),
    USER_DELETE_ADMIN(USER, "user_delete_admin", "管理端用户删除", 3L, Type.UNIT),
    USER_PUT_ROLE_ADMIN(USER, "user_put_role_admin", "管理端设置角色", 4L, Type.UNIT),
    USER_ADMIN_SIMULATION_LOGIN_ADMIN(USER, "user_admin_simulation_login_admin", "管理端模拟登录", 5L, Type.UNIT),

    /**
     * 角色管理
     */
    ROLE("role", "role", "角色管理", 0L, Type.MODULE),
    ROLE_VIEW_ADMIN(ROLE, "role_view_admin", "管理端角色查看", 1L, Type.UNIT),
    ROLE_PUT_ADMIN(ROLE, "role_put_admin", "管理端角色修改", 2L, Type.UNIT),
    ROLE_DELETE_ADMIN(ROLE, "role_delete_admin", "管理端角色删除", 3L, Type.UNIT),
    ROLE_INSERT_ADMIN(ROLE, "role_insert_admin", "管理端添加角色", 4L, Type.UNIT),
    ROLE_PUT_AUTHORITY_ADMIN(ROLE, "role_put_authority_admin", "管理端设置权限", 5L, Type.UNIT),

    /**
     * 权限管理
     */
    AUTHORITY("authority", "authority", "权限管理", 0L, Type.MODULE),
    AUTHORITY_VIEW_ADMIN(AUTHORITY, "authority_view_admin", "管理端权限查看", 1L, Type.UNIT),
    /**
     * 操作日志
     */
    OPERATION_LOG("operation_log", "operation_log", "操作日志", 0L, Type.MODULE),
    OPERATION_LOG_VIEW_ADMIN(OPERATION_LOG, "operation_log_view_admin", "管理端查看操作日志", 1L, Type.UNIT),

//    /**
//     * 咨询反馈
//     */
//    FEEDBACK("feedback", "feedback", "咨询反馈", 0L, Type.MODULE),
//    FEEDBACK_VIEW(FEEDBACK, "feedback_view", "咨询反馈查看", 1L, Type.UNIT),

    /**
     * 企业管理
     */
    ENTERPRISE("enterprise", "enterprise", "企业管理", 0L, Type.MODULE),
    ENTERPRISE_VIEW_ENTERPRISE(ENTERPRISE, "enterprise_view_enterprise", "企业端企业查看", 1L, Type.UNIT),
    ENTERPRISE_PUT_ADMIN(ENTERPRISE, "enterprise_put_admin", "管理端修改企业", 2L, Type.UNIT),
    ENTERPRISE_DELETE_ADMIN(ENTERPRISE, "enterprise_delete_admin", "管理端企业删除", 3L, Type.UNIT),
    ENTERPRISE_INSERT_ADMIN(ENTERPRISE, "enterprise_insert_admin", "管理端添加企业", 4L, Type.UNIT),
    ENTERPRISE_APPROVE_ADMIN(ENTERPRISE, "enterprise_approve_admin", "管理端审批企业", 5L, Type.UNIT),
    ENTERPRISE_USER_ENTERPRISE(ENTERPRISE, "enterprise_user_enterprise", "企业端员工管理", 6L, Type.UNIT),
    ENTERPRISE_PUT_ENTERPRISE(ENTERPRISE, "enterprise_put_enterprise", "企业端修改企业", 7L, Type.UNIT),
    ENTERPRISE_ALIPAY_SECRET_ENTERPRISE(ENTERPRISE, "enterprise_alipay_secret_enterprise", "企业端支付宝支付密钥管理", 8L, Type.UNIT),
    ENTERPRISE_VIEW_ADMIN(ENTERPRISE, "enterprise_view_admin", "管理端企业查看", 9L, Type.UNIT),
    ENTERPRISE_NEWS_ENTERPRISE(ENTERPRISE, "enterprise_news_enterprise", "企业端企业新闻资讯管理", 10L, Type.UNIT),
    ENTERPRISE_PARTNER_ENTERPRISE(ENTERPRISE, "enterprise_partner_enterprise", "企业端企业伙伴管理", 11L, Type.UNIT),
    ENTERPRISE_SERVE_ENTERPRISE(ENTERPRISE, "enterprise_serve_enterprise", "企业端企业服务管理", 12L, Type.UNIT),
    ENTERPRISE_SLIDESHOW_ENTERPRISE(ENTERPRISE, "enterprise_slideshow_enterprise", "企业端企业轮播图管理", 13L, Type.UNIT),
    ENTERPRISE_SPOT_ENTERPRISE(ENTERPRISE, "enterprise_spot_enterprise", "企业端考点管理", 14L, Type.UNIT),
    ENTERPRISE_ANNOUNCEMENT_ENTERPRISE(ENTERPRISE, "enterprise_announcement_enterprise", "企业端报名发布公告", 15L, Type.UNIT),
    ENTERPRISE_DYNAMIC_OPTIONS_ENTERPRISE(ENTERPRISE, "enterprise_dynamic_options_enterprise", "企业端动态选项管理", 16L, Type.UNIT),
    ENTERPRISE_FORM_TEMPLATE_ENTERPRISE(ENTERPRISE, "enterprise_form_template_enterprise", "企业端表单模板管理", 17L, Type.UNIT),
//    /**
//     * 表单模板管理
//     */
//    FORM_TEMPLATE("form_template", "form_template", "表单模板管理", 0L, Type.MODULE),
//    FORM_TEMPLATE_VIEW(FORM_TEMPLATE, "form_template_view", "表单模板查看", 1L, Type.UNIT),

    /**
     * 报名发布管理
     */
    REGISTER_PUBLISH("register_publish", "register_publish", "报名发布管理", 0L, Type.MODULE),
    REGISTER_PUBLISH_VIEW_ENTERPRISE(REGISTER_PUBLISH, "register_publish_view_enterprise", "企业端报名发布查看", 1L, Type.UNIT),
    REGISTER_PUBLISH_INSERT_ENTERPRISE(REGISTER_PUBLISH, "register_publish_insert_enterprise", "企业端报名发布新增", 2L, Type.UNIT),
    REGISTER_PUBLISH_PUT_ENTERPRISE(REGISTER_PUBLISH, "register_publish_put_enterprise", "企业端报名发布修改", 3L, Type.UNIT),
    REGISTER_PUBLISH_DELETE_ENTERPRISE(REGISTER_PUBLISH, "register_publish_delete_enterprise", "企业端报名发布删除", 4L, Type.UNIT),
    REGISTER_PUBLISH_VIEW_ADMIN(REGISTER_PUBLISH, "register_publish_view_admin", "管理端报名发布查看", 1L, Type.UNIT),


    /**
     * 报名信息管理
     */
    REGISTER_INFO("register_info", "register_info", "报名信息管理", 0L, Type.MODULE),
    REGISTER_INFO_APPROVE_ENTERPRISE(REGISTER_INFO, "register_info_approve_enterprise", "企业端报名信息审批", 1L, Type.UNIT),
    REGISTER_INFO_SCORE_ENTERPRISE(REGISTER_INFO, "register_info_score_enterprise", "企业端报名信息成绩", 2L, Type.UNIT),
    REGISTER_INFO_VIEW_ENTERPRISE(REGISTER_INFO, "register_info_view_enterprise", "企业端报名信息查看", 3L, Type.UNIT),

    /**
     * 企业账单管理
     */
    ENTERPRISE_BILL("enterprise_bill", "enterprise_bill", "企业账单管理", 0L, Type.MODULE),
    ENTERPRISE_BILL_VIEW_ENTERPRISE(ENTERPRISE_BILL, "enterprise_bill_view_enterprise", "企业端企业账单查看", 1L, Type.UNIT),


    ;

    private final String module;
    private final AuthorityEnum parent;
    private final String value;
    private final String title;
    private final Long shift;
    private final Type type;
    private final List<AuthorityEnum> children;


    AuthorityEnum(String module, String value, String title, Long shift, Type type) {
        this(module, null, value, title, shift, type);
    }

    AuthorityEnum(AuthorityEnum parent, String value, String title, Long shift, Type type) {
        this(parent.module, parent, value, title, shift, type);
    }

    /**
     * 注：shift 用于逻辑位移的操作数
     */
    AuthorityEnum(String module, AuthorityEnum parent, String value, String title, Long shift, Type type) {
        this.module = module;
        this.parent = parent;
        this.value = value;
        this.title = title;
        if (shift == null) {
            this.shift = 0L;
        } else {
            this.shift = 1L << shift; // 逻辑位移
        }
        this.type = type;
        this.children = new ArrayList<>();
        if (this.parent != null) {
            this.parent.children.add(this);
        }
    }

    public enum Type {
        MODULE, // 功能模块
        UNIT, // 功能单元
    }


    @Override
    public String value() {
        return value;
    }

    @Override
    public String title() {
        return title;
    }

    public Type type() {
        return type;
    }

    public List<AuthorityEnum> children() {
        return children;
    }

    public String module() {
        return module;
    }

    public Long shift() {
        return shift;
    }

    public AuthorityEnum parent() {
        return parent;
    }


    // 访问父类枚举
    public static List<AuthorityEnum> getAllParentEnum() {
        return Arrays.stream(AuthorityEnum.values())
                .filter(value -> Type.MODULE.equals(value.type))
                .collect(Collectors.toList());
    }

    // 访问父类枚举
    public static List<AuthorityEnumModel> getAllParentEnumModel() {
        return getAllParentEnum().stream()
                .map(AuthorityEnumModel::new)
                .collect(Collectors.toList());
    }

}
