package top.gaogle.dao.master;

import org.springframework.stereotype.Repository;
import top.gaogle.pojo.model.EnterpriseNewsModel;
import top.gaogle.pojo.param.EnterpriseNewsEditParam;
import top.gaogle.pojo.param.EnterpriseNewsQueryParam;

import java.util.List;

@Repository
public interface EnterpriseNewsMapper {

    int insert(EnterpriseNewsEditParam editParam);

    List<EnterpriseNewsModel> queryByPageAndCondition(EnterpriseNewsQueryParam queryParam);

    int put(EnterpriseNewsEditParam editParam);

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    EnterpriseNewsModel queryById(String id);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(String id);


    int deleteByIdAndEnterpriseId(String id, String enterpriseId);

    int deleteByEnterpriseId(String enterpriseId);
}
