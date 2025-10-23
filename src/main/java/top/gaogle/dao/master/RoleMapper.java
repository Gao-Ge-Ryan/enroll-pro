package top.gaogle.dao.master;


import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.gaogle.pojo.enums.RoleTypeEnum;
import top.gaogle.pojo.model.RoleModel;
import top.gaogle.pojo.param.RoleEditParam;
import top.gaogle.pojo.param.RoleQueryParam;

import java.util.List;

@Repository
public interface RoleMapper {

    List<RoleModel> getAllRoleList();

    int insert(RoleEditParam editParam);

    List<RoleModel> queryByPageAndCondition(RoleQueryParam queryParam);

    int patchRole(RoleEditParam editParam);

    Integer querySizeByName(String name);

    Integer querySizeExcludeSelfByName(String name,String id);

    int deleteRole(String roleId);

    List<RoleModel> queryAll();

    RoleModel queryDetailByRoleId(String roleId);

    List<RoleModel> queryRoleByAccountBy(String accountBy);

    List<RoleModel> queryRolesByUsernames(List<String> usernames);

    List<RoleModel> queryRolesByUsernamesAndType(@Param("usernames") List<String> usernames, RoleTypeEnum type);

    List<String> queryRoleIdsByType(RoleTypeEnum enterpriseRole);

    List<RoleModel> queryByType(RoleTypeEnum type);
}
