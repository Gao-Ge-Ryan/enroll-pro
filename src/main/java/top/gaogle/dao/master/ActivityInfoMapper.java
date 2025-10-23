package top.gaogle.dao.master;

import org.springframework.stereotype.Repository;
import top.gaogle.pojo.model.ActivityInfoModel;
import top.gaogle.pojo.param.ActivityInfoEditParam;

import java.util.List;

@Repository
public interface ActivityInfoMapper {

    int insert(ActivityInfoEditParam activityInfoEditParam);

    List<ActivityInfoModel> queryByRegisterPublishId(String registerPublishId);

    int enterpriseUpdate(ActivityInfoEditParam activityInfoEditParam);

    int deleteByRegisterPublishId(String registerPublishId);
}
