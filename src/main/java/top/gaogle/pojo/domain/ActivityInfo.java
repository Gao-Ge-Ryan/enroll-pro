package top.gaogle.pojo.domain;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * (ActivityInfo)实体类
 *
 * @author gaogle
 * @since 2025-06-20 10:25:54
 */
public class ActivityInfo implements Serializable {
    private static final long serialVersionUID = 951917291686441819L;

    private String id;

    private BigDecimal scoreProportion;

    private Long activityStartAt;

    private Long activityEndAt;

    private String registerPublishId;
    /**
     * 科目
     */
    private String subject;
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


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getActivityStartAt() {
        return activityStartAt;
    }

    public void setActivityStartAt(Long activityStartAt) {
        this.activityStartAt = activityStartAt;
    }

    public Long getActivityEndAt() {
        return activityEndAt;
    }

    public void setActivityEndAt(Long activityEndAt) {
        this.activityEndAt = activityEndAt;
    }

    public String getRegisterPublishId() {
        return registerPublishId;
    }

    public void setRegisterPublishId(String registerPublishId) {
        this.registerPublishId = registerPublishId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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

    public BigDecimal getScoreProportion() {
        return scoreProportion;
    }

    public void setScoreProportion(BigDecimal scoreProportion) {
        this.scoreProportion = scoreProportion;
    }
}

