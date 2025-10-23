package top.gaogle.pojo.dto;

public class RealAlipayPublicKeySignTypeDTO {

    public RealAlipayPublicKeySignTypeDTO(String realAlipayPublicKey, String signType) {
        this.realAlipayPublicKey = realAlipayPublicKey;
        this.signType = signType;
    }

    private String realAlipayPublicKey;

    private String signType;

    public String getRealAlipayPublicKey() {
        return realAlipayPublicKey;
    }

    public void setRealAlipayPublicKey(String realAlipayPublicKey) {
        this.realAlipayPublicKey = realAlipayPublicKey;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }
}
