package top.gaogle.pojo.param;

import top.gaogle.framework.pojo.SuperQuerying;
import top.gaogle.pojo.domain.RegisterPublish;

public class RegisterPublishQueryParam extends RegisterPublish implements SuperQuerying {

    private String accountBy;

    public String getAccountBy() {
        return accountBy;
    }

    public void setAccountBy(String accountBy) {
        this.accountBy = accountBy;
    }
}
