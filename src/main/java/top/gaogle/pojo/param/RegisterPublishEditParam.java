package top.gaogle.pojo.param;

import top.gaogle.pojo.domain.RegisterPublish;

import java.util.List;

public class RegisterPublishEditParam extends RegisterPublish {

    private Long registerPublishCost;

    private Integer admissionTicketNumberLength;

    private String prefix;

    private List<ActivityInfoEditParam> activityInfoEditParams;


    public Long getRegisterPublishCost() {
        return registerPublishCost;
    }

    public void setRegisterPublishCost(Long registerPublishCost) {
        this.registerPublishCost = registerPublishCost;
    }

    public Integer getAdmissionTicketNumberLength() {
        return admissionTicketNumberLength;
    }

    public void setAdmissionTicketNumberLength(Integer admissionTicketNumberLength) {
        this.admissionTicketNumberLength = admissionTicketNumberLength;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public List<ActivityInfoEditParam> getActivityInfoEditParams() {
        return activityInfoEditParams;
    }

    public void setActivityInfoEditParams(List<ActivityInfoEditParam> activityInfoEditParams) {
        this.activityInfoEditParams = activityInfoEditParams;
    }
}
