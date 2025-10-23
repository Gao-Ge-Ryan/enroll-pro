package top.gaogle.pojo.dto;

import java.math.BigDecimal;

public class UpdateScoreDTO {
    private String registerId;
    private String name;
    private String idNumber;
    private String admissionTicketNumber;
    private BigDecimal score;
    private String activityCompositeScoreMap;
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

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public String getActivityCompositeScoreMap() {
        return activityCompositeScoreMap;
    }

    public void setActivityCompositeScoreMap(String activityCompositeScoreMap) {
        this.activityCompositeScoreMap = activityCompositeScoreMap;
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
