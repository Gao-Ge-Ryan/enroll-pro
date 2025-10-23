package top.gaogle.pojo.domain;

public class AlipaySecret {
    private String alipayAppId;

    private String alipayPrivateKey;

    private String alipayAlipayPublicKey;

    private String alipaySignType;

    private String alipaySalt;

    public String getAlipayAppId() {
        return alipayAppId;
    }

    public void setAlipayAppId(String alipayAppId) {
        this.alipayAppId = alipayAppId;
    }

    public String getAlipayPrivateKey() {
        return alipayPrivateKey;
    }

    public void setAlipayPrivateKey(String alipayPrivateKey) {
        this.alipayPrivateKey = alipayPrivateKey;
    }

    public String getAlipayAlipayPublicKey() {
        return alipayAlipayPublicKey;
    }

    public void setAlipayAlipayPublicKey(String alipayAlipayPublicKey) {
        this.alipayAlipayPublicKey = alipayAlipayPublicKey;
    }

    public String getAlipaySignType() {
        return alipaySignType;
    }

    public void setAlipaySignType(String alipaySignType) {
        this.alipaySignType = alipaySignType;
    }

    public String getAlipaySalt() {
        return alipaySalt;
    }

    public void setAlipaySalt(String alipaySalt) {
        this.alipaySalt = alipaySalt;
    }
}
