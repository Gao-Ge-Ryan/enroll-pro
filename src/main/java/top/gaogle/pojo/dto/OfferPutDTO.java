package top.gaogle.pojo.dto;

public class OfferPutDTO {
    private String registerPublishId;
    private Boolean offerStatusShowFlag;
    private String offerStatusShowExplain;

    public Boolean getOfferStatusShowFlag() {
        return offerStatusShowFlag;
    }

    public void setOfferStatusShowFlag(Boolean offerStatusShowFlag) {
        this.offerStatusShowFlag = offerStatusShowFlag;
    }

    public String getOfferStatusShowExplain() {
        return offerStatusShowExplain;
    }

    public void setOfferStatusShowExplain(String offerStatusShowExplain) {
        this.offerStatusShowExplain = offerStatusShowExplain;
    }

    public String getRegisterPublishId() {
        return registerPublishId;
    }

    public void setRegisterPublishId(String registerPublishId) {
        this.registerPublishId = registerPublishId;
    }
}
