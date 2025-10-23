package top.gaogle.dao.master;

import org.springframework.stereotype.Repository;
import top.gaogle.pojo.enums.DynamicOptionsTypeEnum;
import top.gaogle.pojo.model.DynamicOptionsModel;
import top.gaogle.pojo.param.DynamicOptionsEditParam;
import top.gaogle.pojo.param.DynamicOptionsQueryParam;

import java.util.List;

@Repository
public interface DynamicOptionsMapper {

    int insert(DynamicOptionsEditParam editParam);

    DynamicOptionsModel queryOneById(String id);

    int put(DynamicOptionsEditParam editParam);

    int deleteById(String id);

    List<String> queryIdsByPid(String pid);

    List<DynamicOptionsModel> queryAllByPidAndEnterpriseId(String pid, String enterpriseId, DynamicOptionsTypeEnum type);

    List<DynamicOptionsModel> queryAllByType(String enterpriseId, DynamicOptionsTypeEnum type);

    List<DynamicOptionsModel> clientQueryAllByPidAndEnterpriseId(String pid, String enterpriseId, DynamicOptionsTypeEnum type);


    List<DynamicOptionsModel> queryByCondition(DynamicOptionsQueryParam queryParam);


}
