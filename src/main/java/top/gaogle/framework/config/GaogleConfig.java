package top.gaogle.framework.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 读取项目相关配置
 *
 * @author gaogle
 */
@Component
@ConfigurationProperties(prefix = "gaogle")
public class GaogleConfig {

    private static GaogleConfig instance;

    /**
     * 获取地址开关
     */
    private boolean addressEnabled;
    private boolean registerPublishCostEnabled;
    private String systemName;
    private String iconUrl;
    private String clientUrl;
    private String enterpriseUrl;
    private String adminUrl;
    private String adminBrowserTitle;
    private String clientBrowserTitle;
    private String enterpriseBrowserTitle;
    private String keystorePath;
    private String alipayStorePass;
    private String passAES;
    private String projectSignTime;
    private String adminEmail;
    private String beianNumber;
    private String beianName;
    private String gonganBeianNumber;
    private String gonganBeianName;




    @PostConstruct
    public void init() {
        instance = this;
    }

    public static boolean isAddressEnabled() {
        return instance.addressEnabled;
    }

    public static boolean isRegisterPublishCostEnabled() {
        return instance.registerPublishCostEnabled;
    }

    public void setAddressEnabled(boolean addressEnabled) {
        this.addressEnabled = addressEnabled;
    }

    public void setRegisterPublishCostEnabled(boolean registerPublishCostEnabled) {
        this.registerPublishCostEnabled = registerPublishCostEnabled;
    }

    public static String getSystemName() {
        return instance.systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public static String getClientUrl() {
        return instance.clientUrl;
    }

    public void setClientUrl(String clientUrl) {
        this.clientUrl = clientUrl;
    }

    public static String getKeystorePath() {
        return instance.keystorePath;
    }

    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    public static String getAlipayStorePass() {
        return instance.alipayStorePass;
    }

    public void setAlipayStorePass(String alipayStorePass) {
        this.alipayStorePass = alipayStorePass;
    }

    public static String getPassAES() {
        return instance.passAES;
    }

    public void setPassAES(String passAES) {
        this.passAES = passAES;
    }

    public static String getProjectSignTime() {
        return instance.projectSignTime;
    }

    public void setProjectSignTime(String projectSignTime) {
        this.projectSignTime = projectSignTime;
    }

    public static String getAdminEmail() {
        return instance.adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public static String getIconUrl() {
        return instance.iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public static String getEnterpriseUrl() {
        return instance.enterpriseUrl;
    }

    public void setEnterpriseUrl(String enterpriseUrl) {
        this.enterpriseUrl = enterpriseUrl;
    }

    public static String getAdminUrl() {
        return instance.adminUrl;
    }

    public void setAdminUrl(String adminUrl) {
        this.adminUrl = adminUrl;
    }

    public static String getAdminBrowserTitle() {
        return instance.adminBrowserTitle;
    }

    public void setAdminBrowserTitle(String adminBrowserTitle) {
        this.adminBrowserTitle = adminBrowserTitle;
    }

    public static String getEnterpriseBrowserTitle() {
        return instance.enterpriseBrowserTitle;
    }

    public void setEnterpriseBrowserTitle(String enterpriseBrowserTitle) {
        this.enterpriseBrowserTitle = enterpriseBrowserTitle;
    }

    public static String getClientBrowserTitle() {
        return instance.clientBrowserTitle;
    }

    public void setClientBrowserTitle(String clientBrowserTitle) {
        this.clientBrowserTitle = clientBrowserTitle;
    }

    public static String getBeianNumber() {
        return instance.beianNumber;
    }

    public void setBeianNumber(String beianNumber) {
        this.beianNumber = beianNumber;
    }

    public static String getBeianName() {
        return instance.beianName;
    }

    public void setBeianName(String beianName) {
        this.beianName = beianName;
    }

    public static String getGonganBeianNumber() {
        return instance.gonganBeianNumber;
    }

    public void setGonganBeianNumber(String gonganBeianNumber) {
        this.gonganBeianNumber = gonganBeianNumber;
    }

    public static String getGonganBeianName() {
        return instance.gonganBeianName;
    }

    public void setGonganBeianName(String gonganBeianName) {
        this.gonganBeianName = gonganBeianName;
    }
}
