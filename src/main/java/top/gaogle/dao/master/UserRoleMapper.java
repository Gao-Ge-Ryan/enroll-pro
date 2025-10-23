package top.gaogle.dao.master;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.gaogle.pojo.param.UserRoleEditParam;

import java.util.List;

@Repository
public interface UserRoleMapper {

    int deleteByRoleId(String roleId);

    List<String> queryAccountBysByRoleId(String roleId);

    int insert(UserRoleEditParam editParam);

    int deleteByAccountBy(String accountBy);

    List<String> queryRoleIdByAccountBy(String accountBy);

    int deleteEnterpriseUserByAccountByAndEnterpriseRoleId(String accountBy, @Param("enterpriseRoleIds") List<String> enterpriseRoleIds);

    int deleteByAccountBysAndEnterpriseRoleId(@Param("accountBys") List<String> accountBys, @Param("enterpriseRoleIds") List<String> enterpriseRoleIds);
}

