package top.gaogle.common;

import top.gaogle.controller.DynamicRegisterInfoController;
import top.gaogle.pojo.param.DynamicRegisterInfoEditParam;

import java.util.Arrays;
import java.util.List;

/**
 * 常量类
 *
 * @author gaogle
 * @since 1.0.0
 */
public class RegisterConst {

    private RegisterConst() {
        throw new IllegalStateException(RegisterConst.PROHIBIT_INSTANTIATION);
    }

    // go ge
    public static final String GO_GE = "Go-Ge";
    public static final String PICTURE = "picture";
    public static final String FILE = "file";
    public static final int AUTHENTICATION_EXPIRATION_HOURS = 8;
    // 十分钟的毫秒数
    public static final long TEN_MINUTES_IN_MILLIS = 10 * 60 * 1000;
    // 一年的毫秒数
    public static final long ONE_YEAR_MILLIS = 365L * 24 * 60 * 60 * 1000;
    // 禁止实例化
    public static final String PROHIBIT_INSTANTIATION = "Prohibit Instantiation";
    // 密钥
    public static final String AUTHENTICATION_SECRET = "AUTHENTICATION_SECRET";
    // 颁发时间
    public static final String IAT = "iat";
    // 数字10
    public static final Integer TEN = 10;
    // json请求格式
    public static final String JSON_CONTENT_TYPE = "application/json;charset=UTF-8";
    // mybatis的mapper包路径
    public static final String MYBATIS_MAPPER_SCAN = "top.gaogle.dao";
    // 枚举包
    public static final String ENUM_PACKAGE_PREFIX = "top.gaogle.pojo.enums.";
    // 枚举values方法
    public static final String ENUM_VALUES = "values";
    // 邮箱格式正则表达式
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    // 密码必须包含至少一个字母和一个数字,长度必须在8到20个字符之间,可以包含以下特殊字符（但不是必须的）：@$!%*#?&"
    public static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,20}$";
    // http请求
    public static final String HTTP = "http://";
    /**
     * https请求
     */
    public static final String HTTPS = "https://";

    /**
     * UTF-8 字符集
     */
    public static final String UTF8 = "UTF-8";

    /**
     * GBK 字符集
     */
    public static final String GBK = "GBK";

    /**
     * 注册信息表名
     */
    public static final String REGISTER_INFO_TABLE_NAME = "register_info_";

    /**
     * 斜杠
     */
    public static final String SLASH = "/";

    /**
     * 点
     */
    public static final String DOT = ".";

    /**
     * 短横线
     */
    public static final String DASH = "-";

    /**
     * column 字段
     */
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ID_NUMBER = "id_number";
    public static final String COLUMN_ADMISSION_TICKET_NUMBER = "admission_ticket_number";
    public static final String COLUMN_PHOTO = "photo";
    public static final String COLUMN_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_GENDER = "gender";
    public static final String COLUMN_EDUCATION_LEVEL = "education_level";
    public static final String COLUMN_GRADUATED_UNIVERSITY = "graduated_university";
    public static final String COLUMN_MAJOR = "major";
    public static final String COLUMN_SPOT_ID = "spot_id";
    public static final String COLUMN_SPOT = "spot";
    public static final String COLUMN_SPOT_ADDRESS = "spot_address";
    public static final String COLUMN_ROOM_NUMBER = "room_number";
    public static final String COLUMN_SEAT_NUMBER = "seat_number";
    public static final String COLUMN_ACTIVITY_COMPOSITE_SCORE = "activity_composite_score";
    public static final String COLUMN_SCORE = "score";
    public static final String COLUMN_INTERVIEW_SCORE = "interview_score";
    public static final String COLUMN_FINAL_SCORE = "final_score";
    public static final String COLUMN_INTERVIEW_FLAG = "interview_flag";
    public static final String COLUMN_INTERVIEW_TIME = "interview_time";
    public static final String COLUMN_INTERVIEW_SPOT = "interview_spot";
    public static final String COLUMN_INTERVIEW_SPOT_ADDRESS = "interview_spot_address";
    public static final String COLUMN_OFFER_FLAG = "offer_flag";
    public static final String COLUMN_OFFER_EXPLAIN = "offer_explain";
    public static final String COLUMN_TICKET_DOWNLOAD_COUNT = "ticket_download_count";
    public static final String COLUMN_INTERVIEW_TICKET_DOWNLOAD_COUNT = "interview_ticket_download_count";
    public static final String COLUMN_EMAIL_SEND_COUNT = "email_send_count";
    public static final String COLUMN_PHONE_SEND_COUNT = "phone_send_count";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_APPROVE = "approve";
    public static final String COLUMN_REASON = "reason";
    public static final String COLUMN_JSON_EXTEND = "json_extend";
    public static final String COLUMN_FRONTEND_JSON_EXTEND = "frontend_json_extend";
    public static final String COLUMN_CREATE_BY = "create_by";
    public static final String COLUMN_CREATE_AT = "create_at";
    public static final String COLUMN_UPDATE_BY = "update_by";
    public static final String COLUMN_UPDATE_AT = "update_at";

    /**
     * 预置动态字段Map
     * （重要！重要！）说明：
     * 1、column 字段常量字段值需要修改 例子：public static final String COLUMN_ID_NUMBER = "id_number";
     * 2、DynamicRegisterInfo 实体字段需要对应添加，DynamicRegisterInfoMapper.xml文件映射字段需要添加，sqlAllColumns字段列需要添加
     * 3、报名申请（用户端）和报名信息修改（用户端）{@link DynamicRegisterInfoController#clientApplyInfo(DynamicRegisterInfoEditParam)}
     * 和 {@link DynamicRegisterInfoController#clientUpdateApplyInfo(DynamicRegisterInfoEditParam)}接口，预定义字段初始化问题，修改时需要修改代码
     * 4、PRESET_DYNAMIC_FIELD_MAP 常量Map需要添加
     * 5、creatTable sql需要添加字段
     */
    public static final KeyValue<String, String> PRESET_DYNAMIC_FIELD_MAP = new KeyValue<String, String>()
            .entry("id", "编号")
            .entry("name", "姓名")
            .entry("id_number", "证件号码")
            .entry("admission_ticket_number", "准考证号")
            .entry("photo", "照片")
            .entry("phone_number", "手机号")
            .entry("email", "邮箱")
            .entry("gender", "性别")
            .entry("education_level", "学历")
            .entry("graduated_university", "毕业院校")
            .entry("major", "专业")
            .entry("spot_id", "考点id")
            .entry("spot", "考点")
            .entry("spot_address", "考点地址")
            .entry("room_number", "考场号")
            .entry("seat_number", "座号")
            .entry("activity_composite_score", "每场次笔试汇总成绩")
            .entry("score", "笔试总成绩")
            .entry("interview_score", "面试成绩")
            .entry("final_score", "最终成绩")
            .entry("interview_flag", "是否面试标志")
            .entry("interview_time", "面试时间")
            .entry("interview_spot", "面试地点")
            .entry("interview_spot_address", "面试地点详细地址")
            .entry("offer_flag", "是否拟录用标志")
            .entry("offer_explain", "拟录用说明")
            .entry("ticket_download_count", "准考证件下载次数")
            .entry("interview_ticket_download_count", "面试证件下载次数")
            .entry("email_send_count", "邮件发送次数")
            .entry("phone_send_count", "手机发送次数")
            .entry("status", "状态:0初始化,1有效（正式通过）,2无效,3手动处理")
            .entry("approve", "审核状态:0可编辑状态,1待审核,2审核通过,3审核不通过")
            .entry("reason", "理由")
            .entry("json_extend", "JSON扩展")
            .entry("frontend_json_extend", "前端JSON扩展")
            .entry("create_by", "创建者")
            .entry("create_at", "创建时间")
            .entry("update_by", "修改者")
            .entry("update_at", "修改时间");


    /**
     * key
     */
    public static final String KEY = "key";

    /**
     * value
     */
    public static final String VALUE = "value";

    /**
     * matchType 匹配类型 exact精准
     */
    public static final String MATCH_TYPE = "matchType";

    /**
     * exact 精准
     */
    public static final String MATCH_TYPE_EXACT = "exact";

    /**
     * like 模糊
     */
    public static final String MATCH_TYPE_LIKE = "like";


    /**
     * AS 两边空格
     */
    public static final String AS = " AS ";

    /**
     * 模板 type
     */
    public static final String TEMPLATE_TYPE = "type";

    /**
     * 模板 formatType
     */
    public static final String TEMPLATE_FORMAT_TYPE = "formatType";

    /**
     * 模板 keyValue
     */
    public static final String TEMPLATE_KEY_VALUE = "keyValue";

    /**
     * 模板 key
     */
    public static final String TEMPLATE_KEY = "key";

    /**
     * 模板 remark
     */
    public static final String TEMPLATE_REMARK = "remark";

    /**
     * 模板 rule
     */
    public static final String TEMPLATE_RULE = "rule";

    /**
     * 模板 required
     */
    public static final String TEMPLATE_REQUIRED = "required";

    /**
     * 模板 regex
     */
    public static final String TEMPLATE_REGEX = "regex";

    /**
     * 模板 VARCHAR
     */
    public static final String TEMPLATE_VARCHAR = "VARCHAR";

    /**
     * 模板 VARCHAR
     */
    public static final String TEMPLATE_TEXT = "text";

    /**
     * 模板 timestamp
     */
    public static final String TEMPLATE_TIMESTAMP = "timestamp";

    /**
     * 模板 sort
     */
    public static final String TEMPLATE_SORT = "sort";

    /**
     * 模板 order
     */
    public static final String TEMPLATE_ORDER = "order";

    /**
     * 模板 formatType
     */
    public static final String FORMAT_TYPE_TIMESTAMP = "timestamp";

    /**
     * 模板 formatType
     */
    public static final String FORMAT_TYPE_JSON = "json";

    /**
     * 模板 formatType
     */
    public static final String FORMAT_TYPE_ARRAY_JSON = "array_json";

    /**
     * 模板 formatType
     */
    public static final String FORMAT_TYPE_ARRAY_JSON_ATTACHMENT = "array_json_attachment";

    /**
     * 模板 formatType
     */
    public static final String FORMAT_TYPE_SHOW_VALUE = "showValue";

    /**
     * 模板 formatType
     */
    public static final String FORMAT_TYPE_ATTACHMENT_NAME = "name";

    /**
     * 模板 formatType
     */
    public static final String FORMAT_TYPE_ATTACHMENT_URL = "url";


    /**
     * 企业充值金额列表 单位：分
     */
    public static final List<Long> ENTERPRISE_RECHARGE_AMOUNT_LIST = Arrays.asList(10000L, 20000L, 30000L, 50000L,
            100000L, 200000L, 300000L, 500000L,
            1000000L, 2000000L, 3000000L, 5000000L);


    /**
     * 支付宝支付产品码
     */
    public static final String ALIPAY_PRODUCT_CODE = "FAST_INSTANT_TRADE_PAY";

    /**
     * 防重提交 redis key
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 限流 redis key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * 更新报名发布状态 redis key
     */
    public static final String UPDATE_REGISTER_PUBLISH_STATUS_TASK_KEY = "service:updateRegisterPublishStatusTask";

    /**
     * 定时任务 清除删除的报名发布信息 redis key
     */
    public static final String CLEAN_DELETE_REGISTER_PUBLISH_TASK_KEY = "service:cleanDeleteRegisterPublishTask";

    /**
     * 定时任务 清理操作日志 redis key
     */
    public static final String CLEAN_OPERATE_LOG_TASK_TASK_KEY = "service:cleanOperateLogTask";

    /**
     * startAt
     */
    public static final String START_AT = "startAt";

    /**
     * ticketStartAt
     */
    public static final String TICKET_START_AT = "ticketStartAt";

    /**
     * scoreStartAt
     */
    public static final String SCORE_START_AT = "scoreStartAt";

    /**
     * interviewTicketStartAt
     */
    public static final String INTERVIEW_TICKET_START_AT = "interviewTicketStartAt";

    /**
     * interviewScoreStartAt
     */
    public static final String INTERVIEW_SCORE_START_AT = "interviewScoreStartAt";

    /**
     * 验证码key
     */
    public static final String VERIFICATION_CODE = "verificationCode";

    public static final String STYLE_REGISTER = "Style-Register";

    public static final String AUTO_SEND_REGISTER_PUBLISH_ANNOUNCEMENT_TASK_JOB_GROUP = "autoSendRegisterPublishAnnouncementTask";
    public static final String AUTO_SEND_REGISTER_PUBLISH_ANNOUNCEMENT_TASK_INVOKE_TARGET = "autoSendRegisterPublishAnnouncementTask.task('%s','%s')";

    public static final String REGISTER_PUBLISH_ANNOUNCEMENT_TASK_JOB_GROUP = "registerPublishAnnouncementTask";
    public static final String REGISTER_PUBLISH_ANNOUNCEMENT_TASK_INVOKE_TARGET = "registerPublishAnnouncementTask.task('%s','%s')";


    public static final String GENERAL_HTML = "general.html";
    public static final String ALIPAY_KEYSTORE = "alipay.keystore";
    public static final String ALIPAY_CERT = "alipayCert";


}
