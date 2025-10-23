package top.gaogle.framework.pojo;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class CertificateDTO {

    private PrivateKey privateKey;
    private X509Certificate certificate;

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }
}
