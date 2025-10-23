package top.gaogle.dao.master;

import org.springframework.stereotype.Repository;
import top.gaogle.pojo.model.PublishSpotModel;
import top.gaogle.pojo.param.PublishSpotEditParam;

import java.util.List;

@Repository
public interface PublishSpotMapper {

    int deleteByRegisterPublishId(String registerPublishId);

    int insert(PublishSpotEditParam editParam);

    int deleteByRegisterPublishIdAndSpotInfoId(String registerPublishId, String unbindSpotInfoId);

    int putSpotInfo(PublishSpotEditParam editParam);

    PublishSpotModel queryOneById(String id);

    List<PublishSpotModel> queryByRegisterPublishId(String registerPublishId);

    List<PublishSpotModel> queryByRegisterPublishIdForAllocateSpot(String registerPublishId);

    int deleteByIdAndEnterpriseId(String id, String enterpriseId);

    PublishSpotModel queryBySpotAndRegisterPublishId(String spot, String registerPublishId);

    PublishSpotModel queryExcludeIdBySpotAndRegisterPublishId(String id, String spot, String registerPublishId);
}
