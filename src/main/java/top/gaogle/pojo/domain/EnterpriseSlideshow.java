package top.gaogle.pojo.domain;

import java.io.Serializable;

/**
 * (EnterpriseSlideshow)实体类
 *
 * @author makejava
 * @since 2025-04-13 09:59:50
 */
public class EnterpriseSlideshow implements Serializable {
    private static final long serialVersionUID = -96532663043414107L;
    
    private String id;
    
    private String picture;
    /**
     * 跳转地址
     */
    private String redirectUrl;
    /**
     * 企业id
     */
    private String enterpriseId;
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

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(String enterpriseId) {
        this.enterpriseId = enterpriseId;
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

}

