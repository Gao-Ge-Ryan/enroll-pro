package top.gaogle.pojo.enums;

import top.gaogle.framework.pojo.IndexedEnum;

public enum RegisterPublishAnnouncementTypeEnum implements IndexedEnum<Integer> {

    /**
     * 公告
     */
    OTHER(0, "公告"),

    /**
     * 报名公告
     */
    REGISTER(1, "报名公告"),

    /**
     * 打印证件公告
     */
    TICKET(2, "打印证件公告"),

    /**
     * 查询成绩公告
     */
    SCORE(3, "查询成绩公告"),

    /**
     * 新闻动态公告
     */
    NEWS_UPDATES(4, "新闻动态公告"),

    ;

    private final Integer value;
    private final String title;

    RegisterPublishAnnouncementTypeEnum(Integer value, String title) {
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
