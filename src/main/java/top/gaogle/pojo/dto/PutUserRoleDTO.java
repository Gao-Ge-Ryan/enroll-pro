package top.gaogle.pojo.dto;

import java.util.List;

public class PutUserRoleDTO {

    private String accountBy;
    private List<String> roleIds;

    public String getAccountBy() {
        return accountBy;
    }

    public void setAccountBy(String accountBy) {
        this.accountBy = accountBy;
    }


    public List<String> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<String> roleIds) {
        this.roleIds = roleIds;
    }
}
