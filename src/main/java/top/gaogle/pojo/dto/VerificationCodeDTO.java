package top.gaogle.pojo.dto;

import top.gaogle.pojo.enums.VerificationCodeTypeEnum;

public class VerificationCodeDTO {

    private String email;

    private VerificationCodeTypeEnum codeTypeEnum;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public VerificationCodeTypeEnum getCodeTypeEnum() {
        return codeTypeEnum;
    }

    public void setCodeTypeEnum(VerificationCodeTypeEnum codeTypeEnum) {
        this.codeTypeEnum = codeTypeEnum;
    }
}
