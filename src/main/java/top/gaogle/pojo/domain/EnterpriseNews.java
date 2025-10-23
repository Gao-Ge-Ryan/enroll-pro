package top.gaogle.pojo.domain;

import top.gaogle.pojo.enums.EnterpriseNewsTypeEnum;

import java.io.Serializable;

/**
 * (EnterpriseNews)实体类
 *
 * @author makejava
 * @since 2025-04-13 10:56:19
 */
public class EnterpriseNews implements Serializable {
    private static final long serialVersionUID = 287927376714303752L;

    private String id;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;
    /**
     * 类型1富文本，2视频，3pdf
     */
    private EnterpriseNewsTypeEnum type;
    /**
     * 企业id
     */
    private String enterpriseId;
    /**
     * 封面
     */
    private String cover;
    /**
     * 企业简单描述
     */
    private String description;
    /**
     * 文件地址
     */
    private String fileUrl;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public EnterpriseNewsTypeEnum getType() {
        return type;
    }

    public void setType(EnterpriseNewsTypeEnum type) {
        this.type = type;
    }

    public String getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(String enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
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

