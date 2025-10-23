package top.gaogle.pojo.model;

import top.gaogle.pojo.domain.Enterprise;

public class EnterpriseModel extends Enterprise {

    private Integer onGoingStatusCount;

    public Integer getOnGoingStatusCount() {
        return onGoingStatusCount;
    }

    public void setOnGoingStatusCount(Integer onGoingStatusCount) {
        this.onGoingStatusCount = onGoingStatusCount;
    }
}
