package top.gaogle.dao.master;

import org.springframework.stereotype.Repository;
import top.gaogle.pojo.model.EnterpriseServeModel;
import top.gaogle.pojo.param.EnterpriseServeEditParam;
import top.gaogle.pojo.param.EnterpriseServeQueryParam;

import java.util.List;

@Repository
public interface EnterpriseServeMapper {

    int insert(EnterpriseServeEditParam editParam);

    List<EnterpriseServeModel> queryByPageAndCondition(EnterpriseServeQueryParam queryParam);

    int put(EnterpriseServeEditParam editParam);

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    EnterpriseServeModel queryById(String id);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(String id);


    int deleteByIdAndEnterpriseId(String id, String enterpriseId);

    List<EnterpriseServeModel> queryByEnterpriseId(String enterpriseId);

    int deleteByEnterpriseId(String enterpriseId);
}
