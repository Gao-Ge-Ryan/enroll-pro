package top.gaogle.pojo.dto;

public class UpdateInterviewInfoDTO {
    private String registerId;
    private String name;
    private String idNumber;
    private String admissionTicketNumber;
    /**
     * 是否面试标志：true进入面试，false未进面试
     */
    private Boolean interviewFlag;
    /**
     * 面试时间
     */
    private Long interviewTime;

    /**
     * 面试地点
     */
    private String interviewSpot;
    /**
     * 面试地点详细地址
     */
    private String interviewSpotAddress;
    private String  updateBy;
    private Long updateAt;

    public String getRegisterId() {
        return registerId;
    }

    public void setRegisterId(String registerId) {
        this.registerId = registerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getAdmissionTicketNumber() {
        return admissionTicketNumber;
    }

    public void setAdmissionTicketNumber(String admissionTicketNumber) {
        this.admissionTicketNumber = admissionTicketNumber;
    }

    public Boolean getInterviewFlag() {
        return interviewFlag;
    }

    public void setInterviewFlag(Boolean interviewFlag) {
        this.interviewFlag = interviewFlag;
    }

    public Long getInterviewTime() {
        return interviewTime;
    }

    public void setInterviewTime(Long interviewTime) {
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

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Long getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Long updateAt) {
        this.updateAt = updateAt;
    }
}
