package top.gaogle.dao.master;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.gaogle.pojo.dto.RegisterPublishStatusCountDTO;
import top.gaogle.pojo.dto.RegisterPublishStatusUpdateDTO;
import top.gaogle.pojo.dto.UserRegisterDTO;
import top.gaogle.pojo.enums.RegisterPublishStatusEnum;
import top.gaogle.pojo.model.RegisterPublishModel;
import top.gaogle.pojo.param.RegisterPublishEditParam;
import top.gaogle.pojo.param.RegisterPublishQueryParam;

import java.util.List;

@Repository
public interface RegisterPublishMapper {

    int insert(RegisterPublishEditParam editParam);

    int enterpriseUpdate(RegisterPublishEditParam editParam);

    List<RegisterPublishModel> queryByPageAndCondition(RegisterPublishQueryParam queryParam);

    int deleteById(String id);

    RegisterPublishModel queryOneById(String id);

    RegisterPublishModel queryOneByIdAndEnterpriseId(String id, String enterpriseId);

    int updateAllocateSpotFlagById(String id, boolean allocateSpotFlag, String updateBy, Long updateAt);

    List<RegisterPublishModel> queryAllExcludeStatus(@Param("status") RegisterPublishStatusEnum status);

    int updateStatusById(String id, RegisterPublishStatusEnum status);

    int batchUpdateStatus(List<RegisterPublishStatusUpdateDTO> updates);

    int updateDelFlagByIdAndEnterpriseId(String id,String enterpriseId, boolean delFlag, Long delAt);

    int updateDelFlagToTrueByEnterpriseId(String enterpriseId, Long delAt);

    List<UserRegisterDTO> queryRegisterByPageAndCondition(RegisterPublishQueryParam queryParam);

    List<RegisterPublishStatusCountDTO> queryStatusCount(@Param("enterpriseIds") List<String> enterpriseIds,
                                                         @Param("status") RegisterPublishStatusEnum status);

    List<RegisterPublishModel> queryAllDeleteForUpdate();

    int updateOfferStatusShowFlagAndExplain(String registerPublishId, Boolean offerStatusShowFlag, String offerStatusShowExplain);
}
