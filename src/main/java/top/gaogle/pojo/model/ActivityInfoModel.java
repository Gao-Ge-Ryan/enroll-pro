package top.gaogle.pojo.model;

import top.gaogle.pojo.domain.ActivityInfo;

import java.math.BigDecimal;

public class ActivityInfoModel extends ActivityInfo {

    private BigDecimal activityScore;

    public BigDecimal getActivityScore() {
        return activityScore;
    }

    public void setActivityScore(BigDecimal activityScore) {
        this.activityScore = activityScore;
    }
}
