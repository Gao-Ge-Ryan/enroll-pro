package top.gaogle.pojo.enums;

import top.gaogle.framework.pojo.IndexedEnum;

public enum VerificationCodeTypeEnum implements IndexedEnum<Integer> {

    REGISTER(1, "注册验证码"),
    EMAIL_LOGIN(2, "邮箱登录验证码"),
    RESET_PASSWORDS(3, "重置密码验证码"),

    ;

    private final Integer value;
    private final String title;

    VerificationCodeTypeEnum(Integer value, String title) {
        this.value = value;
        this.title = title;
    }


    @Override
    public Integer value() {
        return value;
    }

    @Override
    public String title() {
        return title;
    }
}
