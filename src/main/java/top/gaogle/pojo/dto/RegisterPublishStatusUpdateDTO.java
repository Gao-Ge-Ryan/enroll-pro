package top.gaogle.pojo.dto;

import top.gaogle.pojo.enums.RegisterPublishStatusEnum;

public class RegisterPublishStatusUpdateDTO {

    private String id;
    private RegisterPublishStatusEnum status;

    public RegisterPublishStatusUpdateDTO(String id, RegisterPublishStatusEnum status) {
        this.id = id;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RegisterPublishStatusEnum getStatus() {
        return status;
    }

    public void setStatus(RegisterPublishStatusEnum status) {
        this.status = status;
    }
}
