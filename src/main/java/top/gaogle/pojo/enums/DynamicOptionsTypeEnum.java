package top.gaogle.pojo.enums;

import top.gaogle.framework.pojo.IndexedEnum;

public enum DynamicOptionsTypeEnum implements IndexedEnum<Integer> {

    PROVINCE_CITY_DISTRICT(1, "省-市-区"),
    DEPARTMENT_POSITION(2, "部门/科室-岗位/职位"),
    NATION(3, "民族"),

    ;

    private final Integer value;
    private final String title;

    DynamicOptionsTypeEnum(Integer value, String title) {
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
