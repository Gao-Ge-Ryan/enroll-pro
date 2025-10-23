package top.gaogle.pojo.enums;

import top.gaogle.framework.pojo.IndexedEnum;

public enum EnterpriseShowStatusEnum implements IndexedEnum<Integer> {

    /**
     * 不在客户端展示
     */
    DISABLE(0, "不在客户端展示"),

    /**
     * 在客户端展示
     */
    ENABLE(1, "在客户端展示"),


    ;

    private final Integer value;
    private final String title;

    EnterpriseShowStatusEnum(Integer value, String title) {
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
