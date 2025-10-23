package top.gaogle.pojo.dto;

import com.alibaba.excel.annotation.ExcelProperty;

public class InterviewScoreTemplateExcelDTO {

    @ExcelProperty(value = "编码")
    private String id;
    /**
     * 姓名
     */
    @ExcelProperty(value = "姓名")
    private String name;

    /**
     * 准考证号
     */
    @ExcelProperty(value = "准考证号")
    private String admissionTicketNumber;

    /**
     * 证件号码
     */
    @ExcelProperty(value = "证件号码")
    private String idNumber;

    /**
     * 成绩
     */
    @ExcelProperty(value = "面试成绩（保留两位小数，缺考：-1）")
    private String interviewScore;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdmissionTicketNumber() {
        return admissionTicketNumber;
    }

    public void setAdmissionTicketNumber(String admissionTicketNumber) {
        this.admissionTicketNumber = admissionTicketNumber;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getInterviewScore() {
        return interviewScore;
    }

    public void setInterviewScore(String interviewScore) {
        this.interviewScore = interviewScore;
    }
}
