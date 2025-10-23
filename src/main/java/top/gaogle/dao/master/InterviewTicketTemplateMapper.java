package top.gaogle.dao.master;

import org.springframework.stereotype.Repository;
import top.gaogle.pojo.enums.FormTemplateFlagEnum;
import top.gaogle.pojo.enums.InterviewTicketTemplateFlagEnum;
import top.gaogle.pojo.model.InterviewTicketTemplateModel;

import java.util.List;

@Repository
public interface InterviewTicketTemplateMapper {


    List<InterviewTicketTemplateModel> queryAllByFormTemplateFlag(FormTemplateFlagEnum formTemplateFlag, String enterpriseId);

    InterviewTicketTemplateModel queryOneByFlag(InterviewTicketTemplateFlagEnum  flag);
}
