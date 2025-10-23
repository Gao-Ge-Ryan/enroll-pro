package top.gaogle.service;

import org.springframework.stereotype.Service;
import top.gaogle.framework.config.GaogleConfig;
import top.gaogle.framework.i18n.I18ResultCode;
import top.gaogle.framework.i18n.I18nResult;
import top.gaogle.pojo.dto.SystemAttributeDTO;

@Service
public class SystemService extends SuperService {
    public I18nResult<SystemAttributeDTO> attribute() {

        I18nResult<SystemAttributeDTO> result = I18nResult.newInstance();
        try {
            SystemAttributeDTO attributeDTO = new SystemAttributeDTO();
            attributeDTO.setSystemName(GaogleConfig.getSystemName());
            attributeDTO.setClientBrowserTitle(GaogleConfig.getClientBrowserTitle());
            attributeDTO.setEnterpriseBrowserTitle(GaogleConfig.getEnterpriseBrowserTitle());
            attributeDTO.setAdminBrowserTitle(GaogleConfig.getAdminBrowserTitle());
            attributeDTO.setIconUrl(GaogleConfig.getIconUrl());
            attributeDTO.setClientUrl(GaogleConfig.getClientUrl());
            attributeDTO.setEnterpriseUrl(GaogleConfig.getEnterpriseUrl());
            attributeDTO.setAdminUrl(GaogleConfig.getAdminUrl());
            attributeDTO.setBeianNumber(GaogleConfig.getBeianNumber());
            attributeDTO.setBeianName(GaogleConfig.getBeianName());
            attributeDTO.setGonganBeianName(GaogleConfig.getGonganBeianName());
            attributeDTO.setGonganBeianNumber(GaogleConfig.getGonganBeianNumber());
            result.succeed().setData(attributeDTO);
        } catch (Exception e) {
            log.error("查询所发生异常:", e);
            result.failed().setMessage(I18ResultCode.MESSAGE, "查询发生异常");
        }
        return result;
    }
}
