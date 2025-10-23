package top.gaogle.pojo.domain;

import top.gaogle.pojo.enums.DynamicOptionsStatusEnum;
import top.gaogle.pojo.enums.DynamicOptionsTypeEnum;

import java.io.Serializable;

/**
 * 省市区表(DynamicOptions)实体类
 *
 * @author gaogle
 * @since 2025-06-23 14:24:21
 */
public class DynamicOptions implements Serializable {
    private static final long serialVersionUID = 999192707577260395L;

    private String id;
    /**
     * 名称
     */
    private String name;
    /**
     * 父类id
     */
    private String pid;
    /**
     * 企业id
     */
    private String enterpriseId;

    private DynamicOptionsStatusEnum status;
    /**
     * 类型
     */
    private DynamicOptionsTypeEnum type;

    /**
     * 报名项目id
     */
    private String registerPublishId;

    /**
     * 简单描述
     */
    private String description;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(String enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public DynamicOptionsStatusEnum getStatus() {
        return status;
    }

    public void setStatus(DynamicOptionsStatusEnum status) {
        this.status = status;
    }

    public DynamicOptionsTypeEnum getType() {
        return type;
    }

    public void setType(DynamicOptionsTypeEnum type) {
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

    public String getRegisterPublishId() {
        return registerPublishId;
    }

    public void setRegisterPublishId(String registerPublishId) {
        this.registerPublishId = registerPublishId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

