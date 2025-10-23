package top.gaogle.pojo.param;

import top.gaogle.pojo.domain.AlipaySecret;

public class AlipaySecretEditParam extends AlipaySecret {

    private String enterpriseId;

    public String getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(String enterpriseId) {
        this.enterpriseId = enterpriseId;
    }
}
