package top.gaogle.pojo.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;

import java.util.Date;

public class InterviewTemplateExcelDTO {

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
     * 是否面试标志：true进入面试，false未进面试
     */
    @ExcelProperty(value = "面试标志（true进入面试，false未进面试）")
    private Boolean interviewFlag;

    /**
     * 面试时间
     */
    @ExcelProperty(value = "面试时间（格式：yyyy-MM-dd HH:mm:ss，例：2025-08-25 14:29:00）")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date interviewTime;

    /**
     * 面试地点
     */
    @ExcelProperty(value = "面试地点")
    private String interviewSpot;

    /**
     * 面试地点详细地址
     */
    @ExcelProperty(value = "面试地点详细地址")
    private String interviewSpotAddress;

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

    public Boolean getInterviewFlag() {
        return interviewFlag;
    }

    public void setInterviewFlag(Boolean interviewFlag) {
        this.interviewFlag = interviewFlag;
    }

    public Date getInterviewTime() {
        return interviewTime;
    }

    public void setInterviewTime(Date interviewTime) {
        this.interviewTime = interviewTime;
    }

    public String getInterviewSpot() {
        return interviewSpot;
    }

    public void setInterviewSpot(String interviewSpot) {
        this.interviewSpot = interviewSpot;
    }

    public String getInterviewSpotAddress() {
        return interviewSpotAddress;
    }

    public void setInterviewSpotAddress(String interviewSpotAddress) {
        this.interviewSpotAddress = interviewSpotAddress;
    }
}
