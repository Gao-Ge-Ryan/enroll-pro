package top.gaogle.dao.master;

import org.springframework.stereotype.Repository;
import top.gaogle.pojo.model.SpotInfoModel;
import top.gaogle.pojo.param.SpotInfoEditParam;
import top.gaogle.pojo.param.SpotInfoQueryParam;

import java.util.List;

@Repository
public interface SpotInfoMapper {

    int insert(SpotInfoEditParam editParam);

    int putSpotInfo(SpotInfoEditParam editParam);

    List<SpotInfoModel> queryByPageAndCondition(SpotInfoQueryParam queryParam);

    int deleteById(String id);

    SpotInfoModel queryOneById(String id);

    List<SpotInfoModel> queryByEnableAndEnterpriseId(String enterpriseId);


    SpotInfoModel queryBySpotAndEnterpriseId(String spot, String enterpriseId);

    SpotInfoModel queryExcludeIdBySpotAndEnterpriseId(String id, String spot, String enterpriseId);

    int deleteByIdAndEnterpriseId(String id, String enterpriseId);

    int deleteByEnterpriseId(String id);
}
