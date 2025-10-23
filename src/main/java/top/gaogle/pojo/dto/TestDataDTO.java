package top.gaogle.pojo.dto;

public class TestDataDTO {
    private Integer code;
    private String reason;
    private String secret;


    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "TestDataDTO{" +
                "code=" + code +
                ", reason='" + reason + '\'' +
                ", secret='" + secret + '\'' +
                '}';
    }
}
