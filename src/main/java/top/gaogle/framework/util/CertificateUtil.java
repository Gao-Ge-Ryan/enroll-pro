package top.gaogle.framework.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.CastUtils;
import top.gaogle.framework.exception.LoadKeyStoreException;
import top.gaogle.framework.pojo.CertificateDTO;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;


/**
 * 数字证书工具
 * <p>
 * keytool -storepass gaogle -genkeypair -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -validity 7 -alias alipayCert -keystore alipay.keystore -dname "CN=www.enrollpro.top, OU=enrollpro, O=enrollpro, L=Beijing, ST=Beijing, C=CN"
 * </p>
 *
 * @author gaogle
 */
public class CertificateUtil {

    private static final Logger logger = LoggerFactory.getLogger(CertificateUtil.class);


    public static CertificateDTO loadCertificate(String keyStoreFile, String password, String alias) {
        CertificateDTO certificateDTO = new CertificateDTO();
        try {
            KeyStore keyStore = loadKeyStore(keyStoreFile, password);
            // 读取私钥:
            PrivateKey privateKey = CastUtils.cast(keyStore.getKey(alias, password.toCharArray()));
            // 读取证书:
            X509Certificate certificate = CastUtils.cast(keyStore.getCertificate(alias));
            certificateDTO.setCertificate(certificate);
            certificateDTO.setPrivateKey(privateKey);
        } catch (Exception e) {
            logger.error("加载alipay.keystore失败:", e);
        }
        return certificateDTO;
    }

    public static KeyStore loadKeyStore(String keyStoreFile, String password) {
        try (InputStream input = new FileInputStream(keyStoreFile)) {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(input, password.toCharArray());
            return ks;
        } catch (Exception e) {
            throw new LoadKeyStoreException("加载KeyStore异常");
        }
    }

    public static byte[] encrypt(X509Certificate certificate, byte[] message) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(certificate.getPublicKey().getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, certificate.getPublicKey());
        return cipher.doFinal(message);
    }

    public static byte[] decrypt(PrivateKey privateKey, byte[] data) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    public static byte[] sign(PrivateKey privateKey, X509Certificate certificate, byte[] message)
            throws GeneralSecurityException {
        Signature signature = Signature.getInstance(certificate.getSigAlgName());
        signature.initSign(privateKey);
        signature.update(message);
        return signature.sign();
    }

    public static boolean verify(X509Certificate certificate, byte[] message, byte[] sig) throws GeneralSecurityException {
        Signature signature = Signature.getInstance(certificate.getSigAlgName());
        signature.initVerify(certificate);
        signature.update(message);
        return signature.verify(sig);
    }

    // 公共加密方法
    public static byte[] encryptByPBE(String password, byte[] salt, byte[] input) throws GeneralSecurityException {
        KeyAndIV keyAndIV = deriveKeyAndIV(password, salt);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keyAndIV.key, keyAndIV.iv);
        return cipher.doFinal(input);
    }

    // 公共解密方法
    public static byte[] decryptByPBE(String password, byte[] salt, byte[] encrypted) throws GeneralSecurityException {
        KeyAndIV keyAndIV = deriveKeyAndIV(password, salt);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keyAndIV.key, keyAndIV.iv);
        return cipher.doFinal(encrypted);
    }

    // 抽取的工具方法：派生密钥和 IV
    private static KeyAndIV deriveKeyAndIV(String password, byte[] salt) throws GeneralSecurityException {
        int iterationCount = 999;
        int keyLength = 128;

        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, keyLength * 2); // 128 for key + 128 for IV
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyIvBytes = skf.generateSecret(keySpec).getEncoded();

        byte[] keyBytes = Arrays.copyOfRange(keyIvBytes, 0, 16);  // AES-128密钥
        byte[] ivBytes = Arrays.copyOfRange(keyIvBytes, 16, 32);  // 128-bit IV
        return new KeyAndIV(new SecretKeySpec(keyBytes, "AES"), new IvParameterSpec(ivBytes));
    }

    // 内部类用于封装 Key 和 IV
    private static class KeyAndIV {
        SecretKeySpec key;
        IvParameterSpec iv;

        KeyAndIV(SecretKeySpec key, IvParameterSpec iv) {
            this.key = key;
            this.iv = iv;
        }
    }

    // 将私钥编码为字符串
    public static String privateKeyToString(PrivateKey privateKey) {
        byte[] encoded = privateKey.getEncoded(); // PKCS#8 格式
        return Base64.getEncoder().encodeToString(encoded);
    }

    // 将证书编码为字符串
    public static String certificateToString(X509Certificate certificate) throws CertificateEncodingException {
        byte[] encoded = certificate.getEncoded(); // X.509 格式
        return Base64.getEncoder().encodeToString(encoded);
    }

    public static PrivateKey stringToPrivateKey(String base64Key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] decoded = Base64.getDecoder().decode(base64Key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // 或 "EC" 等视加密类型而定
        return keyFactory.generatePrivate(keySpec);
    }

    public static X509Certificate stringToCertificate(String base64Cert) throws CertificateException {
        byte[] decoded = Base64.getDecoder().decode(base64Cert);
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(decoded));
    }

}
