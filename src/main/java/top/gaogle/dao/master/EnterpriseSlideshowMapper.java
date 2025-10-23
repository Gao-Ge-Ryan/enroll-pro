package top.gaogle.dao.master;

import org.springframework.stereotype.Repository;
import top.gaogle.pojo.model.EnterpriseSlideshowModel;
import top.gaogle.pojo.param.EnterpriseSlideshowEditParam;
import top.gaogle.pojo.param.EnterpriseSlideshowQueryParam;

import java.util.List;

@Repository
public interface EnterpriseSlideshowMapper {

    int insert(EnterpriseSlideshowEditParam editParam);

    List<EnterpriseSlideshowModel> queryByPageAndCondition(EnterpriseSlideshowQueryParam queryParam);

    int put(EnterpriseSlideshowEditParam editParam);

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    EnterpriseSlideshowModel queryById(String id);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(String id);


    int deleteByIdAndEnterpriseId(String id, String enterpriseId);


    List<EnterpriseSlideshowModel> queryByEnterpriseId(String enterpriseId);

    int deleteByEnterpriseId(String enterpriseId);
}
