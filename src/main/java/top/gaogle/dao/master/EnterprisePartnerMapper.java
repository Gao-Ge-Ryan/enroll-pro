package top.gaogle.dao.master;

import org.springframework.stereotype.Repository;
import top.gaogle.pojo.model.EnterprisePartnerModel;
import top.gaogle.pojo.param.EnterprisePartnerEditParam;
import top.gaogle.pojo.param.EnterprisePartnerQueryParam;

import java.util.List;

@Repository
public interface EnterprisePartnerMapper {

    int insert(EnterprisePartnerEditParam editParam);

    List<EnterprisePartnerModel> queryByPageAndCondition(EnterprisePartnerQueryParam queryParam);

    int put(EnterprisePartnerEditParam editParam);

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    EnterprisePartnerModel queryById(String id);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(String id);


    int deleteByIdAndEnterpriseId(String id, String enterpriseId);

    List<EnterprisePartnerModel> queryByEnterpriseId(String enterpriseId);

    int deleteByEnterpriseId(String enterpriseId);
}
