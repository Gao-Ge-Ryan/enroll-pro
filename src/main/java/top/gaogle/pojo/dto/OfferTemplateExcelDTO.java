package top.gaogle.pojo.dto;

import com.alibaba.excel.annotation.ExcelProperty;

public class OfferTemplateExcelDTO {

    @ExcelProperty(value = "编码")
    private String id;
    /**
     * 姓名
     */
    @ExcelProperty(value = "姓名")
    private String name;

    /**
     * 准考证号
     */
    @ExcelProperty(value = "准考证号")
    private String admissionTicketNumber;

    /**
     * 证件号码
     */
    @ExcelProperty(value = "证件号码")
    private String idNumber;

    /**
     * 拟录用标志（true拟录用，false未录用
     */
    @ExcelProperty(value = "拟录用标志（true拟录用，false未录用）")
    private Boolean offerFlag;


    /**
     * 拟录用说明（选填）
     */
    @ExcelProperty(value = "拟录用说明（选填）")
    private String offerExplain;



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdmissionTicketNumber() {
        return admissionTicketNumber;
    }

    public void setAdmissionTicketNumber(String admissionTicketNumber) {
        this.admissionTicketNumber = admissionTicketNumber;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getOfferExplain() {
        return offerExplain;
    }

    public void setOfferExplain(String offerExplain) {
        this.offerExplain = offerExplain;
    }

    public Boolean getOfferFlag() {
        return offerFlag;
    }

    public void setOfferFlag(Boolean offerFlag) {
        this.offerFlag = offerFlag;
    }
}
