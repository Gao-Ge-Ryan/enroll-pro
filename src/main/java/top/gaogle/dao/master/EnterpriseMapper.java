package top.gaogle.dao.master;

import org.springframework.stereotype.Repository;
import top.gaogle.pojo.dto.ApproveDTO;
import top.gaogle.pojo.enums.EnterpriseShowStatusEnum;
import top.gaogle.pojo.enums.EnterpriseStatusEnum;
import top.gaogle.pojo.model.AlipaySecretModel;
import top.gaogle.pojo.model.EnterpriseModel;
import top.gaogle.pojo.model.EnterpriseUserModel;
import top.gaogle.pojo.param.AlipaySecretEditParam;
import top.gaogle.pojo.param.EnterpriseEditParam;
import top.gaogle.pojo.param.EnterpriseQueryParam;
import top.gaogle.pojo.param.EnterpriseUserEditParam;

import java.util.List;

@Repository
public interface EnterpriseMapper {

    int insert(EnterpriseEditParam editParam);

    List<EnterpriseModel> queryByPageAndCondition(EnterpriseQueryParam queryParam);

    int putEnterprise(EnterpriseEditParam editParam);

    int deleteById(String id);

    EnterpriseModel queryOneById(String id);

    List<EnterpriseModel> queryAllByAndCondition(EnterpriseQueryParam queryParam);

    EnterpriseModel queryByCreateBy(String loginUsername);

    EnterpriseModel queryByAccountBy(String accountBy);

    EnterpriseModel queryByAccountByAndStatus(String accountBy, EnterpriseStatusEnum status);

    List<EnterpriseModel> clientQueryByPage(EnterpriseQueryParam queryParam);

    int enterprisePutEnterprise(EnterpriseEditParam queryParam);

    EnterpriseModel clientQueryEnterprise(String enterpriseId);

    long queryBalanceById(String enterpriseId);

    int updateBalanceById(String enterpriseId, long lastBalance);

    List<EnterpriseUserModel> queryUserByEnterpriseId(String enterpriseId);

    AlipaySecretModel queryAlipaySecretByEnterpriseId(String enterpriseId);

    int insertUser(EnterpriseUserEditParam editParam);

    EnterpriseUserModel queryUserByAccountBy(String accountBy);

    int deleteUserByAccountBy(String accountBy);

    int putEnterpriseAlipaySecret(AlipaySecretEditParam editParam);

    int updateStatusAndReasonById(String enterpriseId, EnterpriseStatusEnum status, String reason);

    int approveInfoUpdate(ApproveDTO approveDTO);

    List<String> queryAccountByByEnterpriseId(String enterpriseId);

    int updateShowStatusById(String enterpriseId, EnterpriseShowStatusEnum showStatus);

}
