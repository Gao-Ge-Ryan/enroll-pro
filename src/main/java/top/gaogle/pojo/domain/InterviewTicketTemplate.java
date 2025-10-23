package top.gaogle.pojo.domain;

import top.gaogle.pojo.enums.TicketTemplateFlagEnum;
import top.gaogle.pojo.enums.TicketTemplateRangeEnum;
import top.gaogle.pojo.enums.TicketTemplateStatusEnum;

import java.io.Serializable;

/**
 * (TicketTemplate)实体类
 *
 * @author makejava
 * @since 2025-06-20 22:50:25
 */
public class InterviewTicketTemplate implements Serializable {
    private static final long serialVersionUID = 724365752369656824L;

    private String id;

    private String title;

    private String description;

    private String fieldContent;
    /**
     * 文本内容
     */
    private String textContent;

    private String enterpriseId;
    /**
     * 范围
     */
    private TicketTemplateRangeEnum range;
    /**
     * 状态
     */
    private TicketTemplateStatusEnum status;
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

    private TicketTemplateFlagEnum flag;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFieldContent() {
        return fieldContent;
    }

    public void setFieldContent(String fieldContent) {
        this.fieldContent = fieldContent;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
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


    public void setRange(TicketTemplateRangeEnum range) {
        this.range = range;
    }

    public TicketTemplateStatusEnum getStatus() {
        return status;
    }

    public void setStatus(TicketTemplateStatusEnum status) {
        this.status = status;
    }

    public TicketTemplateFlagEnum getFlag() {
        return flag;
    }

    public void setFlag(TicketTemplateFlagEnum flag) {
        this.flag = flag;
    }
}

