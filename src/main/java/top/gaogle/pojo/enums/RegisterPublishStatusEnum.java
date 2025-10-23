package top.gaogle.pojo.enums;

import top.gaogle.framework.pojo.IndexedEnum;

public enum RegisterPublishStatusEnum implements IndexedEnum<Integer> {

    NEW(0, "新建"),
    WAITING_TO_REGISTER(1, "等待报名开始"),
    REGISTRATION_ONGOING(2, "报名进行中"),
    AWAITING_ID_PRINTING(3, "等待打印笔试证件开始"),
    PRINT_EXAM_ADMISSION_TICKET(4, "打印笔试证件进行中"),
    WAITING_FOR_THE_EXAM(5, "等待活动开始"),
    THE_EXAM_IS_IN_PROGRESS(6, "活动进行中"),
    WAITING_FOR_RESULT_INQUIRY(7, "等待笔试成绩查询开始"),
    SCORE_INQUIRY(8, "笔试成绩查询进行中"),
    WAITING_FOR_INTERVIEW_TICKET(9, "等待打印面试证件开始"),
    PRINT_FOR_INTERVIEW_TICKET(10, "打印面试证件进行中"),
    WAITING_FOR_INTERVIEW_SCORE(11, "等待面试成绩查询开始"),
    INTERVIEW_SCORE_INQUIRY(12, "面试成绩查询进行中"),
    COMPLETED(13, "已结束"),
    ARCHIVE(14, "归档"),

    ;

    private final Integer value;
    private final String title;

    RegisterPublishStatusEnum(Integer value, String title) {
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
