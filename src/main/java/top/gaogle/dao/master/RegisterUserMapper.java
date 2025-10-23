package top.gaogle.dao.master;

import org.springframework.stereotype.Repository;
import top.gaogle.pojo.param.RegisterUserEditParam;

@Repository
public interface RegisterUserMapper {

    int insert(RegisterUserEditParam editParam);

    int deleteByRegisterPublishId(String registerPublishId);
}
