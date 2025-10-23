package top.gaogle.pojo.param;


import top.gaogle.framework.pojo.SuperQuerying;
import top.gaogle.pojo.domain.User;

import java.util.List;

public class UserQueryParam extends User implements SuperQuerying {

    private String roleId;
    private List<String> usernames;

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public List<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(List<String> usernames) {
        this.usernames = usernames;
    }
}
