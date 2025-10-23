package top.gaogle.pojo.model;

import top.gaogle.pojo.domain.RegisterPublish;

import java.util.List;

public class RegisterPublishModel extends RegisterPublish {

    private Integer registerInfoValidCount;

    private String strCost;

    private Long userRegisterAt;

    private List<ActivityInfoModel> activityInfoModels;

    public Integer getRegisterInfoValidCount() {
        return registerInfoValidCount;
    }

    public void setRegisterInfoValidCount(Integer registerInfoValidCount) {
        this.registerInfoValidCount = registerInfoValidCount;
    }

    public String getStrCost() {
        return strCost;
    }

    public void setStrCost(String strCost) {
        this.strCost = strCost;
    }

    public Long getUserRegisterAt() {
        return userRegisterAt;
    }

    public void setUserRegisterAt(Long userRegisterAt) {
        this.userRegisterAt = userRegisterAt;
    }

    public List<ActivityInfoModel> getActivityInfoModels() {
        return activityInfoModels;
    }

    public void setActivityInfoModels(List<ActivityInfoModel> activityInfoModels) {
        this.activityInfoModels = activityInfoModels;
    }
}
