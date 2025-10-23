package top.gaogle.pojo.enums;

import top.gaogle.framework.pojo.IndexedEnum;

public enum EnterpriseStatusEnum implements IndexedEnum<Integer> {

    /**
     * 可编辑状态
     */
    EDITABLE(0, "可编辑状态"),

    /**
     * 待审核
     */
    PENDING(1, "待审核"),

    /**
     * 审核通过
     */
    APPROVED(2, "审核通过"),

    /**
     * 审核不通过
     */
    REJECTED(3, "审核不通过"),
    ;

    private final Integer value;
    private final String title;

    EnterpriseStatusEnum(Integer value, String title) {
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
