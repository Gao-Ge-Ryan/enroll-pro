package top.gaogle.pojo.domain;

import top.gaogle.pojo.enums.EnterpriseShowStatusEnum;
import top.gaogle.pojo.enums.EnterpriseStatusEnum;

import java.io.Serializable;

/**
 * (Enterprise)实体类
 *
 * @author makejava
 * @since 2024-06-28 15:38:36
 */
public class Enterprise implements Serializable {
    private static final long serialVersionUID = -56828590399627636L;

    private String id;

    private String name;

    private String logo;

    private String reason;
    /**
     * 状态
     */
    private EnterpriseStatusEnum status;
    /**
     * 显示状态
     */
    private EnterpriseShowStatusEnum showStatus;

    private String description;
    /**
     * 信用代码
     */
    private String creditCode;
    /**
     * 余额，单位分
     */
    private Long balance;
    /**
     * 创建者
     */
    private String createBy;
    /**
     * 创建时间
     */
    private Long createAt;
    /**
     * 修改者
     */
    private String updateBy;
    /**
     * 修改时间
     */
    private Long updateAt;

    /**
     * 轮播图
     */
    private String slideshow;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 联系手机
     */
    private String contactPhone;

    /**
     * 工作时间
     */
    private String workTime;

    /**
     * 工作地址
     */
    private String workAddress;

    /**
     * 申请手机号
     */
    private String applyPhone;


    /**
     * 是否展示文化
     */
    private Boolean culturePhilosophyFlag;

    /**
     * 文化理念
     */
    private String culturePhilosophy;

    /**
     * 是否展示合作伙伴
     */
    private Boolean partnerFlag;

    /**
     * 关注微信公众号
     */
    private String subscribeWechat;

    /**
     * 是否展示服务
     */
    private Boolean serveFlag;

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

    public EnterpriseStatusEnum getStatus() {
        return status;
    }

    public void setStatus(EnterpriseStatusEnum status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreditCode() {
        return creditCode;
    }

    public void setCreditCode(String creditCode) {
        this.creditCode = creditCode;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
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

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSlideshow() {
        return slideshow;
    }

    public void setSlideshow(String slideshow) {
        this.slideshow = slideshow;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getWorkTime() {
        return workTime;
    }

    public void setWorkTime(String workTime) {
        this.workTime = workTime;
    }

    public String getWorkAddress() {
        return workAddress;
    }

    public void setWorkAddress(String workAddress) {
        this.workAddress = workAddress;
    }

    public String getApplyPhone() {
        return applyPhone;
    }

    public void setApplyPhone(String applyPhone) {
        this.applyPhone = applyPhone;
    }

    public Boolean getCulturePhilosophyFlag() {
        return culturePhilosophyFlag;
    }

    public void setCulturePhilosophyFlag(Boolean culturePhilosophyFlag) {
        this.culturePhilosophyFlag = culturePhilosophyFlag;
    }

    public String getCulturePhilosophy() {
        return culturePhilosophy;
    }

    public void setCulturePhilosophy(String culturePhilosophy) {
        this.culturePhilosophy = culturePhilosophy;
    }

    public Boolean getPartnerFlag() {
        return partnerFlag;
    }

    public void setPartnerFlag(Boolean partnerFlag) {
        this.partnerFlag = partnerFlag;
    }

    public String getSubscribeWechat() {
        return subscribeWechat;
    }

    public void setSubscribeWechat(String subscribeWechat) {
        this.subscribeWechat = subscribeWechat;
    }

    public Boolean getServeFlag() {
        return serveFlag;
    }

    public void setServeFlag(Boolean serveFlag) {
        this.serveFlag = serveFlag;
    }

    public EnterpriseShowStatusEnum getShowStatus() {
        return showStatus;
    }

    public void setShowStatus(EnterpriseShowStatusEnum showStatus) {
        this.showStatus = showStatus;
    }
}

