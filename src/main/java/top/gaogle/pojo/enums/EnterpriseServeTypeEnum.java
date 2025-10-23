package top.gaogle.pojo.enums;

import top.gaogle.framework.pojo.IndexedEnum;

public enum EnterpriseServeTypeEnum implements IndexedEnum<Integer> {
    /**
     * 富文本
     */
    RICH_TEXT(1, "富文本"),

    /**
     * 跳转连接
     */
    REDIRECT_URL(2, "跳转链接");


    private final Integer value;
    private final String title;

    EnterpriseServeTypeEnum(Integer value, String title) {
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
