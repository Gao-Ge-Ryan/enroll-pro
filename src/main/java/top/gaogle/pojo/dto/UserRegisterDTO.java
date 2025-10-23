package top.gaogle.pojo.dto;

import top.gaogle.pojo.enums.RegisterPublishStatusEnum;

public class UserRegisterDTO {

    private String id;

    private String title;

    /**
     * 状态
     */
    private RegisterPublishStatusEnum status;

    /**
     * 创建时间
     */
    private Long createAt;

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

    public RegisterPublishStatusEnum getStatus() {
        return status;
    }

    public void setStatus(RegisterPublishStatusEnum status) {
        this.status = status;
    }

    public Long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Long createAt) {
        this.createAt = createAt;
    }
}
