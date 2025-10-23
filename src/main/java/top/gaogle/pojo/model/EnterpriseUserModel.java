package top.gaogle.pojo.model;

import top.gaogle.pojo.domain.EnterpriseUser;

import java.util.List;

public class EnterpriseUserModel extends EnterpriseUser {

    List<RoleModel> roleModels;

    public List<RoleModel> getRoleModels() {
        return roleModels;
    }

    public void setRoleModels(List<RoleModel> roleModels) {
        this.roleModels = roleModels;
    }
}
