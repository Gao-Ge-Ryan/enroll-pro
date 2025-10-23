package top.gaogle.pojo.domain;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.converters.string.StringImageConverter;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;

public class DynamicRegisterInfo {
    @ExcelIgnore
    private String registerPublishId;
    @ExcelIgnore
    private ObjectNode objectNode;

    @ExcelProperty(value = "编码")
    private String id;
    /**
     * 姓名
     */
    @ExcelProperty(value = "姓名")
    private String name;
    /**
     * 证件号码
     */
    @ExcelProperty(value = "证件号码")
    private String idNumber;
    /**
     * 准考证号
     */
    @ExcelProperty(value = "准考证号")
    private String admissionTicketNumber;
    /**
     * 照片
     */
    @ExcelProperty(value = "照片",converter = StringImageConverter.class)
    private String photo;
    /**
     * 手机号
     */
    @ExcelProperty(value = "手机号")
    private String phoneNumber;
    /**
     * 邮箱
     */
    @ExcelProperty(value = "邮箱")
    private String email;

    /**
     * 性别
     */
    @ExcelProperty(value = "性别")
    private String gender;

    /**
     * 学历
     */
    @ExcelProperty(value = "学历")
    private String educationLevel;

    /**
     * 毕业院校
     */
    @ExcelProperty(value = "毕业院校")
    private String graduatedUniversity;

    /**
     * 专业
     */
    @ExcelProperty(value = "专业")
    private String major;

    /**
     * 考点id
     */
    @ExcelProperty(value = "考点id")
    private String spotId;

    /**
     * 考点
     */
    @ExcelProperty(value = "考点")
    private String spot;
    /**
     * 考点地址
     */
    @ExcelProperty(value = "考点地址")
    private String spotAddress;
    /**
     * 考场号
     */
    @ExcelProperty(value = "考场号")
    private String roomNumber;
    /**
     * 座号
     */
    @ExcelProperty(value = "座号")
    private String seatNumber;

    /**
     * 每场次笔试汇总成绩
     */
    @ExcelProperty(value = "每场次笔试汇总成绩")
    private String activityCompositeScore;

    /**
     * 笔试总成绩
     */
    @ExcelProperty(value = "笔试总成绩")
    private BigDecimal score;

    /**
     * 面试成绩
     */
    @ExcelProperty(value = "面试成绩")
    private BigDecimal interviewScore;

    /**
     * 最终成绩
     */
    @ExcelProperty(value = "最终成绩")
    private BigDecimal finalScore;

    /**
     * 是否面试标志
     */
    @ExcelProperty(value = "是否面试标志")
    private Boolean interviewFlag;

    /**
     * 面试时间
     */
    @ExcelProperty(value = "面试时间")
    private Long interviewTime;

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


    /**
     * 是否拟录用标志
     */
    @ExcelProperty(value = "是否拟录用标志")
    private Boolean offerFlag;

    /**
     * 拟录用说明
     */
    @ExcelProperty(value = "拟录用说明")
    private String offerExplain;


    /**
     * 证件下载次数
     */
    @ExcelProperty(value = "证件下载次数")
    private Integer ticketDownloadCount;

    /**
     * 面试证件下载次数
     */
    @ExcelProperty(value = "面试证件下载次数")
    private Integer interviewTicketDownloadCount;

    /**
     * 邮件发送次数
     */
    @ExcelProperty(value = "邮件发送次数")
    private Integer emailSendCount;

    /**
     * 手机发送次数
     */
    @ExcelProperty(value = "手机发送次数")
    private Integer phoneSendCount;

    /**
     * 状态:0初始化,1有效（正式通过）,2无效,3手动处理
     */
    @ExcelProperty(value = "状态:0初始化,1有效（正式通过）,2无效,3手动处理")
    private Integer status;
    /**
     * 审核状态:0可编辑状态,1待审核,2审核通过,3审核不通过
     */
    @ExcelProperty(value = "审核状态:0可编辑状态,1待审核,2审核通过,3审核不通过")
    private Integer approve;
    /**
     * 理由
     */
    @ExcelProperty(value = "理由")
    private String reason;

    /**
     * JSON扩展
     */
    @ExcelProperty(value = "JSON扩展")
    private String jsonExtend;

    /**
     * 前端JSON扩展
     */
    @ExcelProperty(value = "前端JSON扩展")
    private String frontendJsonExtend;

    /**
     * 创建者
     */
    @ExcelProperty(value = "创建者")
    private String createBy;
    /**
     * 创建时间
     */
    @DateTimeFormat("yyyy年MM月dd日HH时mm分ss秒")
    @ExcelProperty(value = "创建时间")
    private Long createAt;
    /**
     * 修改者
     */
    @ExcelProperty(value = "修改者")
    private String updateBy;
    /**
     * 修改时间
     */
    @DateTimeFormat("yyyy年MM月dd日HH时mm分ss秒")
    @ExcelProperty(value = "修改时间")
    private Long updateAt;

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

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSpot() {
        return spot;
    }

    public void setSpot(String spot) {
        this.spot = spot;
    }

    public String getSpotAddress() {
        return spotAddress;
    }

    public void setSpotAddress(String spotAddress) {
        this.spotAddress = spotAddress;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }


    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getActivityCompositeScore() {
        return activityCompositeScore;
    }

    public void setActivityCompositeScore(String activityCompositeScore) {
        this.activityCompositeScore = activityCompositeScore;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public BigDecimal getInterviewScore() {
        return interviewScore;
    }

    public void setInterviewScore(BigDecimal interviewScore) {
        this.interviewScore = interviewScore;
    }

    public BigDecimal getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(BigDecimal finalScore) {
        this.finalScore = finalScore;
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

    public Boolean getOfferFlag() {
        return offerFlag;
    }

    public void setOfferFlag(Boolean offerFlag) {
        this.offerFlag = offerFlag;
    }

    public String getOfferExplain() {
        return offerExplain;
    }

    public void setOfferExplain(String offerExplain) {
        this.offerExplain = offerExplain;
    }

    public Integer getInterviewTicketDownloadCount() {
        return interviewTicketDownloadCount;
    }

    public void setInterviewTicketDownloadCount(Integer interviewTicketDownloadCount) {
        this.interviewTicketDownloadCount = interviewTicketDownloadCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getApprove() {
        return approve;
    }

    public void setApprove(Integer approve) {
        this.approve = approve;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Long createAt) {
        this.createAt = createAt;
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

    public String getRegisterPublishId() {
        return registerPublishId;
    }

    public void setRegisterPublishId(String registerPublishId) {
        this.registerPublishId = registerPublishId;
    }

    public ObjectNode getObjectNode() {
        return objectNode;
    }

    public void setObjectNode(ObjectNode objectNode) {
        this.objectNode = objectNode;
    }

    public String getSpotId() {
        return spotId;
    }

    public void setSpotId(String spotId) {
        this.spotId = spotId;
    }

    public String getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public String getGraduatedUniversity() {
        return graduatedUniversity;
    }

    public void setGraduatedUniversity(String graduatedUniversity) {
        this.graduatedUniversity = graduatedUniversity;
    }

    public Integer getTicketDownloadCount() {
        return ticketDownloadCount;
    }

    public void setTicketDownloadCount(Integer ticketDownloadCount) {
        this.ticketDownloadCount = ticketDownloadCount;
    }

    public Integer getEmailSendCount() {
        return emailSendCount;
    }

    public void setEmailSendCount(Integer emailSendCount) {
        this.emailSendCount = emailSendCount;
    }

    public Integer getPhoneSendCount() {
        return phoneSendCount;
    }

    public void setPhoneSendCount(Integer phoneSendCount) {
        this.phoneSendCount = phoneSendCount;
    }

    public String getJsonExtend() {
        return jsonExtend;
    }

    public void setJsonExtend(String jsonExtend) {
        this.jsonExtend = jsonExtend;
    }

    public String getFrontendJsonExtend() {
        return frontendJsonExtend;
    }

    public void setFrontendJsonExtend(String frontendJsonExtend) {
        this.frontendJsonExtend = frontendJsonExtend;
    }
}
