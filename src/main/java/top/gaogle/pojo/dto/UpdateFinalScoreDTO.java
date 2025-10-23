package top.gaogle.pojo.dto;

import java.math.BigDecimal;

public class UpdateFinalScoreDTO {
    private String id;
    private BigDecimal finalScore;

    private String updateBy;
    private Long updateAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(BigDecimal finalScore) {
        this.finalScore = finalScore;
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
