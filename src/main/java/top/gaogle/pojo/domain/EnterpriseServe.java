package top.gaogle.pojo.domain;

import top.gaogle.pojo.enums.EnterpriseServeTypeEnum;

import java.io.Serializable;

/**
 * (EnterpriseServe)实体类
 *
 * @author makejava
 * @since 2025-04-10 10:37:28
 */
public class EnterpriseServe implements Serializable {
    private static final long serialVersionUID = -40200521173405741L;
    
    private String id;
    /**
     * 图标
     */
    private String logo;
    
    private String name;
    /**
     * 简单描述
     */
    private String description;
    /**
     * 跳转地址
     */
    private String redirectUrl;
    /**
     * 富文本
     */
    private String richText;
    /**
     * 1富文本、2跳转链接
     */
    private EnterpriseServeTypeEnum type;
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
    
    private String enterpriseId;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
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

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getRichText() {
        return richText;
    }

    public void setRichText(String richText) {
        this.richText = richText;
    }

    public EnterpriseServeTypeEnum getType() {
        return type;
    }

    public void setType(EnterpriseServeTypeEnum type) {
        this.type = type;
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

    public String getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(String enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

}

