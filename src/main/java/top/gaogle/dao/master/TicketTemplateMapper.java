package top.gaogle.dao.master;

import org.springframework.stereotype.Repository;
import top.gaogle.pojo.enums.FormTemplateFlagEnum;
import top.gaogle.pojo.enums.TicketTemplateFlagEnum;
import top.gaogle.pojo.model.TicketTemplateModel;

import java.util.List;

@Repository
public interface TicketTemplateMapper {



    List<TicketTemplateModel> queryAllByFormTemplateFlag(FormTemplateFlagEnum formTemplateFlag, String enterpriseId);

    TicketTemplateModel queryOneByFlag(TicketTemplateFlagEnum flag);
}
