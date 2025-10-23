package top.gaogle.pojo.enums;

import top.gaogle.framework.pojo.IndexedEnum;

public enum FormTemplateStatusEnum implements IndexedEnum<Integer> {
    /**
     * 未上线
     */
    NOT_ONLINE(0, "未上线"),

    /**
     * 上线
     */
    ONLINE(1, "上线"),


    ;

    private final Integer value;
    private final String title;

    FormTemplateStatusEnum(Integer value, String title) {
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
