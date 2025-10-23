package top.gaogle.pojo.enums;

import top.gaogle.framework.pojo.IndexedEnum;

public enum RegisterInfoStatusEnum implements IndexedEnum<Integer> {
    /**
     * 初始化
     */
    INIT(0, "初始化"),

    /**
     * 有效 （正式通过）
     */
    VALID(1, "有效"),

    /**
     * 无效
     */
    INVALID(2, "无效"),

    /**
     * 人工处理
     */
    MANUAL_PROCESSING(3, "人工处理"),
    ;

    private final Integer value;
    private final String title;

    RegisterInfoStatusEnum(Integer value, String title) {
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
