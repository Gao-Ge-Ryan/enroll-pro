package top.gaogle.pojo.enums;

import top.gaogle.framework.pojo.IndexedEnum;

public enum EnterpriseNewsTypeEnum implements IndexedEnum<Integer> {

    /**
     * 富文本
     */
    RICH_TEXT(1, "富文本"),

    /**
     * 视频
     */
    VIDEO(2, "视频"),

    /**
     * pdf
     */
    PDF(3, "pdf");


    private final Integer value;
    private final String title;

    EnterpriseNewsTypeEnum(Integer value, String title) {
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
