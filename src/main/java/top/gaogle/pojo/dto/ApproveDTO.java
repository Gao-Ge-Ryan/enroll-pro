package top.gaogle.pojo.dto;

import top.gaogle.pojo.enums.EnterpriseStatusEnum;

public class ApproveDTO {

    private String enterpriseId;

    private String logo;

    private String name;

    private String description;

    private String applyPhone;

    private EnterpriseStatusEnum status;

    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApplyPhone() {
        return applyPhone;
    }

    public void setApplyPhone(String applyPhone) {
        this.applyPhone = applyPhone;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public EnterpriseStatusEnum getStatus() {
        return status;
    }

    public void setStatus(EnterpriseStatusEnum status) {
        this.status = status;
    }

    public String getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(String enterpriseId) {
        this.enterpriseId = enterpriseId;
    }
}
